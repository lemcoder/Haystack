package io.github.lemcoder.haystack.core.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.asAssistantMessage
import ai.koog.agents.core.dsl.extension.executeTool
import ai.koog.agents.core.dsl.extension.requestLLM
import ai.koog.agents.core.environment.result
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
import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.koog.edge.cactus.CactusModels
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

    sealed class AgentState {
        data object Uninitialized : AgentState()
        data object Initializing : AgentState()
        data class Ready(val availableNeedles: List<String>) : AgentState()
        data class Processing(val toolCalls: List<String> = emptyList()) : AgentState()
        data class Completed(val response: String) : AgentState()
        data class Error(val message: String) : AgentState()
    }

    suspend fun initializeAgent() {
        try {
            _agentState.value = AgentState.Initializing

            val needles = needleRepository.getAllNeedles()
            currentNeedles = needles

            val settings = settingsRepository.settingsFlow.first()
            val cactusExecutor = SingleLLMPromptExecutor(getCactusLLMClient(context))

            // Create tool registry with all needles as tools
            val toolRegistry = ToolRegistry {
                needles.forEach { needle ->
                    tool(NeedleToolAdapter(needle))
                }
            }

            // Create functional strategy for chat interactions
            val strategy = functionalStrategy<String, String>("haystack-chat") { input ->
                val toolCalls = mutableListOf<String>()

                llm.writeSession {
                    appendPrompt {
                        user(input)
                    }
                }

                var response = requestLLM(input)

                // Handle tool calls
                while (response is Message.Tool.Call) {
                    val toolName = response.tool
                    toolCalls.add(toolName)

                    // Update state with current tool call
                    _agentState.value = AgentState.Processing(toolCalls)

                    Log.d(TAG, "Executing tool: $toolName")
                    val result = executeTool(response)
                    Log.d(TAG, "Tool result: ${result.result}")

                    llm.writeSession {
                        appendPrompt {
                            tool {
                                result(result)
                            }
                        }
                        response = requestLLM()
                    }
                }

                // Get final assistant message
                response.asAssistantMessage().content
            }

            // Create system prompt
            val systemPrompt = buildSystemPrompt(needles)

            // Create agent config
            val agentConfig = AIAgentConfig(
                prompt = prompt(
                    "haystack-chat",
                    params = settingsRepository.toCactusLLMParams(settings)
                ) {
                    system(systemPrompt)
                },
                model = CactusModels.Chat.Qwen3_0_6B,
                maxAgentIterations = 10,
            )

            // Create the agent
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

    suspend fun runAgent(input: String): String {
        val agent = currentAgent
            ?: throw IllegalStateException("Agent not initialized")

        return try {
            _agentState.value = AgentState.Processing()
            val response = agent.run(input)
            _agentState.value = AgentState.Completed(response)
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

    private fun buildSystemPrompt(needles: List<Needle>): String {
        val needleDescriptions = needles.joinToString("\n") { needle ->
            "- ${needle.name}: ${needle.description}"
        }

        return """You are a helpful AI assistant with access to various tools called "Needles".
            |
            |Available Needles:
            |$needleDescriptions
            |
            |When a user asks you to perform a task:
            |1. Analyze which Needle(s) can help accomplish the task
            |2. Call the appropriate Needle(s) with the correct parameters
            |3. Use the results to provide a clear, helpful response to the user
            |
            |Always explain what you're doing and show your results clearly.
            |If you need to use multiple Needles, use them sequentially.
        """.trimMargin()
    }

    companion object {
        private const val TAG = "ChatAgentService"

        val Instance: ChatAgentService by lazy {
            ChatAgentService()
        }
    }
}
