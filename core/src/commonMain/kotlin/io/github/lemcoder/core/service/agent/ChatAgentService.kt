package io.github.lemcoder.core.service.agent

import ai.koog.agents.core.agent.AIAgent
import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.data.repository.PromptExecutorRepository
import io.github.lemcoder.core.exception.ExecutorNotSelectedException
import io.github.lemcoder.core.model.llm.toPromptExecutor
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult
import io.github.lemcoder.core.utils.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Service that manages the chat agent lifecycle and state. */
internal class ChatAgentService(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance,
    private val promptExecutorRepository: PromptExecutorRepository =
        PromptExecutorRepository.Instance,
    private val agentConfigFactory: AgentConfigFactory = AgentConfigFactory(),
    private val toolRegistryFactory: ToolRegistryFactory = ToolRegistryFactory(),
    private val chatStrategyFactory: ChatStrategyFactory = ChatStrategyFactory(),
) {
    private val _agentState = MutableStateFlow<AgentState>(AgentState.Uninitialized)
    val agentState: StateFlow<AgentState> = _agentState.asStateFlow()
    private var currentAgent: AIAgent<String, String>? = null
    private var currentNeedles: List<Needle> = emptyList()
    private var onNeedleResultCallback: ((Result<NeedleResult>) -> Unit)? = null

    /** Set callback to receive needle execution results */
    fun setNeedleResultCallback(callback: (Result<NeedleResult>) -> Unit) {
        onNeedleResultCallback = callback
    }

    suspend fun initializeAgent() {
        try {
            val executorConfig =
                promptExecutorRepository.getSelectedExecutor()
                    ?: throw ExecutorNotSelectedException()
            val executor = executorConfig.toPromptExecutor()
            _agentState.update { AgentState.Initializing }

            val needles = needleRepository.getVisibleNeedles()
            currentNeedles = needles

            currentAgent =
                AIAgent(
                    promptExecutor = executor,
                    strategy = chatStrategyFactory.createChatStrategy(agentState = _agentState),
                    agentConfig = agentConfigFactory.createAgentConfig(executorConfig),
                    toolRegistry =
                        toolRegistryFactory.createToolRegistry(
                            needles = needles,
                            onNeedleResult = onNeedleResultCallback,
                        ),
                )

            _agentState.value = AgentState.Ready(needles.map { it.name })
            Log.d(TAG, "Agent initialized with ${needles.size} needles")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize agent", e)
            _agentState.value = AgentState.Error("Failed to initialize: ${e.message}")
        }
    }

    suspend fun runAgent(input: String): String {
        val agent = currentAgent ?: throw IllegalStateException("Agent not initialized")

        return try {
            _agentState.update { AgentState.Processing() }
            val response = agent.run(input)

            // Update state with completed response
            _agentState.update { AgentState.Completed(response) }

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
