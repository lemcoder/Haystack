package io.github.lemcoder.haystack.presentation.screen.chat

import android.util.Log
import androidx.lifecycle.viewModelScope
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.github.lemcoder.haystack.core.data.NeedleRepository
import io.github.lemcoder.haystack.core.data.SettingsRepository
import io.github.lemcoder.haystack.core.koog.NeedleToolAdapter
import io.github.lemcoder.haystack.core.model.chat.Message
import io.github.lemcoder.haystack.core.model.chat.MessageRole
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance,
    private val settingsRepository: SettingsRepository = SettingsRepository.Instance,
    private val navigationService: NavigationService = NavigationService.Instance,
) : MviViewModel<ChatState, ChatEvent>() {

    private val _state = MutableStateFlow(ChatState())
    override val state: StateFlow<ChatState> = _state.asStateFlow()

    private var agent: AIAgent? = null

    init {
        loadNeedles()
        initializeAgent()
    }

    private fun loadNeedles() {
        viewModelScope.launch {
            needleRepository.needlesFlow.collect { needles ->
                _state.value = _state.value.copy(
                    availableNeedles = needles.map { it.name }
                )
                // Reinitialize agent when needles change
                initializeAgent()
            }
        }
    }

    private fun initializeAgent() {
        viewModelScope.launch {
            try {
                val needles = needleRepository.getAllNeedles()
                val settings = settingsRepository.settingsFlow.first()

                // Create tool registry with needle adapters
                val toolRegistry = ToolRegistry {
                    needles.forEach { needle ->
                        tool(NeedleToolAdapter(needle))
                    }
                }

                // Create LLM model  
                val llmModel = LLModel(
                    provider = LLMProvider.Cactus,
                    id = "local-model",
                    contextLength = 8192,
                    capabilities = listOf(
                        LLMCapability.Completion,
                        LLMCapability.Tools
                    )
                )

                // Create system prompt
                val systemPrompt =
                    """You are a helpful assistant with access to various tools (called Needles).
                    |When a user asks you to do something, analyze which tools are available and use them appropriately.
                    |Available tools: ${needles.joinToString(", ") { it.name }}
                    |
                    |Always explain what you're doing and show your results clearly.
                """.trimMargin()

                // Create agent config
                val agentConfig = AIAgentConfig(
                    systemPrompt = systemPrompt,
                    model = llmModel,
                    temperature = settings.temperature ?: 0.7,
                    maxAgentIterations = 10
                )

                // Create executor
                val executor = CactusPromptExecutor(
                    params = settingsRepository.toCactusLLMParams(settings)
                )

                // Create agent with event handling
                agent = AIAgent(
                    executor = executor,
                    agentConfig = agentConfig,
                    toolRegistry = toolRegistry,
                    installFeatures = {
                        handleEvents {
                            onToolCallStarting { event ->
                                Log.d(TAG, "Tool called: ${event.tool.name}")
                            }
                            onAgentExecutionFailed { event ->
                                Log.e(TAG, "Agent error", event.throwable)
                            }
                        }
                    }
                )

                Log.d(TAG, "Agent initialized with ${needles.size} needles")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize agent", e)
                _state.value = _state.value.copy(
                    errorMessage = "Failed to initialize AI: ${e.message}"
                )
            }
        }
    }

    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.UpdateInput -> {
                _state.value = _state.value.copy(currentInput = event.input)
            }

            ChatEvent.SendMessage -> sendMessage()
            ChatEvent.ClearChat -> clearChat()
            ChatEvent.OpenSettings -> navigationService.navigateTo(Destination.Settings)
            ChatEvent.OpenNeedles -> navigationService.navigateTo(Destination.Needles)
        }
    }

    private fun sendMessage() {
        val input = _state.value.currentInput.trim()
        if (input.isBlank() || agent == null) return

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isProcessing = true,
                    errorMessage = null
                )

                // Add user message
                val userMessage = Message(
                    id = UUID.randomUUID().toString(),
                    content = input,
                    role = MessageRole.USER
                )
                _state.value = _state.value.copy(
                    messages = _state.value.messages + userMessage,
                    currentInput = ""
                )

                // Run agent
                val response = agent!!.run(input)

                // Add assistant message
                val assistantMessage = Message(
                    id = UUID.randomUUID().toString(),
                    content = response,
                    role = MessageRole.ASSISTANT
                )
                _state.value = _state.value.copy(
                    messages = _state.value.messages + assistantMessage,
                    isProcessing = false
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = "Error: ${e.message}"
                )
                SnackbarUtil.showSnackbar("Error: ${e.message}")
            }
        }
    }

    private fun clearChat() {
        _state.value = _state.value.copy(messages = emptyList())
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
