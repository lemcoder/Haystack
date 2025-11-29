package io.github.lemcoder.haystack.core.service.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.asAssistantMessage
import ai.koog.agents.core.dsl.extension.requestLLM
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.message.Message
import android.content.Context
import android.util.Log
import io.github.lemcoder.haystack.App
import io.github.lemcoder.haystack.core.data.NeedleRepository
import io.github.lemcoder.haystack.core.data.SettingsRepository
import io.github.lemcoder.haystack.core.koog.NeedleToolAdapter
import io.github.lemcoder.haystack.core.model.llm.consts.BaseLocalModel
import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.service.needle.NeedleToolExecutor
import io.github.lemcoder.koog.edge.cactus.getCactusLLMClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Service that manages the chat agent lifecycle and state.
 */
class ChatAgentService(
    private val context: Context = App.context,
    private val needleRepository: NeedleRepository = NeedleRepository.Instance,
    private val settingsRepository: SettingsRepository = SettingsRepository.Instance,
) {
    private val _agentState = MutableStateFlow<AgentState>(AgentState.Uninitialized)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()

    private var currentAgent: AIAgent<String, String>? = null
    private var currentNeedles: List<Needle> = emptyList()
    private var onNeedleResultCallback: ((NeedleToolExecutor.NeedleResult) -> Unit)? = null
    private val needleExecutor = NeedleToolExecutor()

    /**
     * Set callback to receive needle execution results
     */
    fun setNeedleResultCallback(callback: (NeedleToolExecutor.NeedleResult) -> Unit) {
        onNeedleResultCallback = callback
    }

    suspend fun initializeAgent() {
        try {
            _agentState.value = AgentState.Initializing

            // Dynamically load needles from repository
            val needles = needleRepository.getAllNeedles()
            currentNeedles = needles

            val settings = settingsRepository.settingsFlow.first()
            val cactusExecutor = SingleLLMPromptExecutor(getCactusLLMClient(context))

            // Create new tool registry with dynamically loaded needles
            val toolRegistry = createToolRegistry(needles)

            // Create functional strategy for chat interactions
            val strategy = createChatStrategy()

            // Create agent config without system message
            val agentConfig = AIAgentConfig(
                prompt = prompt(
                    "haystack-chat",
                    params = settingsRepository.toCactusLLMParams(settings)
                ) {
                    // No system message - will be added later based on experimentation
                },
                model = BaseLocalModel,
                maxAgentIterations = 10,
            )

            // Create the agent with new tool registry
            currentAgent = AIAgent(
                promptExecutor = cactusExecutor,
                strategy = strategy,
                agentConfig = agentConfig,
                toolRegistry = toolRegistry,
            )

            _agentState.value = AgentState.Ready(needles.map { it.name })
            Log.d(TAG, "Agent initialized with ${needles.size} needles")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize agent", e)
            _agentState.value = AgentState.Error("Failed to initialize: ${e.message}")
        }
    }

    /**
     * Creates a new tool registry with all available needles as tools
     */
    private fun createToolRegistry(needles: List<Needle>): ToolRegistry {
        return ToolRegistry {
            needles.forEach { needle ->
                tool(NeedleToolAdapter(needle))
            }
        }
    }

    /**
     * Creates the functional strategy for handling chat interactions with tool calls
     */
    private fun createChatStrategy() =
        functionalStrategy<String, String>("haystack-chat") { input ->
            val toolCalls = mutableListOf<String>()

            llm.writeSession {
                appendPrompt {
                    user(input)
                }
            }

            var response = requestLLM(input)

            // Handle tool calls with our custom executor
            if (response is Message.Tool.Call) {
                val toolName = response.tool
                toolCalls.add(toolName)

                // Update state with current tool call
                _agentState.value = AgentState.Processing(toolCalls)

                Log.d(TAG, "Executing tool: $toolName")

                // Find the needle and execute it
                val needle = findNeedleByToolName(toolName)
                if (needle != null) {
                    // Execute needle with our custom executor
                    val needleResult = needleExecutor.executeNeedle(response, needle)

                    Log.d(TAG, "Needle execution completed: $needleResult")

                    // Emit needle result via callback
                    onNeedleResultCallback?.invoke(needleResult)

                    // For MVP, end workflow here - return the result as string
                    return@functionalStrategy when (needleResult) {
                        is NeedleToolExecutor.NeedleResult.StringResult -> needleResult.value
                        is NeedleToolExecutor.NeedleResult.IntResult -> needleResult.value.toString()
                        is NeedleToolExecutor.NeedleResult.FloatResult -> needleResult.value.toString()
                        is NeedleToolExecutor.NeedleResult.BooleanResult -> needleResult.value.toString()
                        is NeedleToolExecutor.NeedleResult.ImageResult -> "Image saved at: ${needleResult.imagePath}"
                        is NeedleToolExecutor.NeedleResult.AnyResult -> needleResult.value.toString()
                        is NeedleToolExecutor.NeedleResult.ErrorResult -> "Error: ${needleResult.error}"
                    }
                } else {
                    Log.e(TAG, "Needle not found for tool: $toolName")
                    return@functionalStrategy "Error: Tool not found"
                }
            }

            // Get final assistant message if no tool was called
            response.asAssistantMessage().content
        }

    /**
     * Finds a needle by its tool name (lowercase with underscores)
     */
    private fun findNeedleByToolName(toolName: String): Needle? {
        return currentNeedles.find { needle ->
            needle.name.replace(" ", "_").lowercase() == toolName
        }
    }

    suspend fun runAgent(input: String): String {
        val agent = currentAgent
            ?: throw IllegalStateException("Agent not initialized")

        return try {
            _agentState.value = AgentState.Processing()
            val response = agent.run(input)

            // Update state with completed response
            _agentState.value = AgentState.Completed(response)

            // For MVP, response is already a string representation
            // The actual typed result was already sent via callback
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error running agent", e)
            val errorMessage = "Error: ${e.message}"
            _agentState.value = AgentState.Error(errorMessage)
            errorMessage
        }
    }

    fun isReady(): Boolean {
        return currentAgent != null && _agentState.value is AgentState.Ready
    }

    companion object {
        private const val TAG = "ChatAgentService"

        val Instance: ChatAgentService by lazy {
            ChatAgentService()
        }
    }
}
