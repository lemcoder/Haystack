package io.github.lemcoder.core.service.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.asAssistantMessage
import ai.koog.agents.core.dsl.extension.requestLLM
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.data.repository.PromptExecutorRepository
import io.github.lemcoder.core.exception.ExecutorNotSelectedException
import io.github.lemcoder.core.koog.NeedleToolAdapter
import io.github.lemcoder.core.model.llm.toPromptExecutor
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.needle.NeedleArgumentParser
import io.github.lemcoder.core.needle.NeedleResult
import io.github.lemcoder.core.needle.NeedleToolExecutor
import io.github.lemcoder.core.needle.toDisplayString
import io.github.lemcoder.core.utils.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Service that manages the chat agent lifecycle and state. */
internal class ChatAgentService(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance,
    private val executorRepository: PromptExecutorRepository = PromptExecutorRepository.Instance,
) {
    private val _agentState = MutableStateFlow<AgentState>(AgentState.Uninitialized)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()
    private var currentAgent: AIAgent<String, String>? = null
    private var currentNeedles: List<Needle> = emptyList()
    private var onNeedleResultCallback: ((Result<NeedleResult>) -> Unit)? = null
    private val needleExecutor = NeedleToolExecutor()

    /** Set callback to receive needle execution results */
    fun setNeedleResultCallback(callback: (Result<NeedleResult>) -> Unit) {
        onNeedleResultCallback = callback
    }

    suspend fun initializeAgent() {
        try {
            val executorConfig =
                executorRepository.getSelectedExecutor() ?: throw ExecutorNotSelectedException()
            val executor = executorConfig.toPromptExecutor()
            _agentState.update { AgentState.Initializing }

            val needles = needleRepository.getVisibleNeedles()
            currentNeedles = needles

            currentAgent =
                AIAgent(
                    promptExecutor = executor,
                    strategy = createChatStrategy(),
                    agentConfig = createAgentConfig(OpenRouterModels.GPT_OSS_120b),
                    toolRegistry = createToolRegistry(needles),
                )

            _agentState.value = AgentState.Ready(needles.map { it.name })
            Log.d(TAG, "Agent initialized with ${needles.size} needles")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize agent", e)
            _agentState.value = AgentState.Error("Failed to initialize: ${e.message}")
        }
    }

    /** Creates a new tool registry with all available needles as tools */
    private fun createToolRegistry(needles: List<Needle>): ToolRegistry {
        return ToolRegistry { needles.forEach { needle -> tool(NeedleToolAdapter(needle)) } }
    }

    /** Creates the functional strategy for handling chat interactions with tool calls */
    private fun createChatStrategy() =
        functionalStrategy<String, String>("haystack-chat") { input ->
            val toolCalls = mutableListOf<String>()
            val response = requestLLM(input)

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
                    val params = NeedleArgumentParser().parseArguments(response, needle)
                    val needleResult = needleExecutor.executeNeedle(params, needle)

                    Log.d(TAG, "Needle execution completed: $needleResult")

                    // Emit needle result via callback
                    onNeedleResultCallback?.invoke(needleResult)

                    // For MVP, end workflow here - return the result as string
                    return@functionalStrategy needleResult.fold(
                        onSuccess = { result ->
                            result.toDisplayString() // Convert the typed result to string
                        },
                        onFailure = { error -> "Error: ${error.message}" },
                    )
                } else {
                    Log.e(TAG, "Needle not found for tool: $toolName")
                    return@functionalStrategy "Error: Tool not found"
                }
            }

            // Get final assistant message if no tool was called
            response.asAssistantMessage().content
        }

    private fun createAgentConfig(model: LLModel): AIAgentConfig {
        return AIAgentConfig(
            prompt =
                prompt("haystack-chat") {
                    system("You are an AI assistant that helps users by calling tools as needed.")
                },
            model = model,
            maxAgentIterations = 10,
        )
    }

    /** Finds a needle by its tool name (lowercase with underscores) */
    private fun findNeedleByToolName(toolName: String): Needle? {
        return currentNeedles.find { needle ->
            needle.name.replace(" ", "_").lowercase() == toolName
        }
    }

    suspend fun runAgent(input: String): String {
        val agent = currentAgent ?: throw IllegalStateException("Agent not initialized")

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

        val Instance: ChatAgentService by lazy { ChatAgentService() }
    }
}
