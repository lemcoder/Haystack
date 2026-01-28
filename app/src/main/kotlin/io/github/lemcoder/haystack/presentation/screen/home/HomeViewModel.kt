package io.github.lemcoder.haystack.presentation.screen.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.model.chat.Message
import io.github.lemcoder.core.model.chat.MessageContentType
import io.github.lemcoder.core.model.chat.MessageRole
import io.github.lemcoder.core.needle.toDisplayString
import io.github.lemcoder.core.service.agent.AgentState
import io.github.lemcoder.core.useCase.ObserveChatAgentStateUseCase
import io.github.lemcoder.core.useCase.RunChatAgentUseCase
import io.github.lemcoder.core.useCase.needle.ObserveNeedlesUseCase
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val navigationService: NavigationService = NavigationService.Instance,
    private val runChatAgentUseCase: RunChatAgentUseCase = RunChatAgentUseCase.create(),
    private val observeChatAgentStateUseCase: ObserveChatAgentStateUseCase =
        ObserveChatAgentStateUseCase.create(),
    private val observeNeedlesUseCase: ObserveNeedlesUseCase = ObserveNeedlesUseCase.create(),
) : MviViewModel<HomeState, HomeEvent>() {
    private val _state = MutableStateFlow(HomeState())
    override val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeNeedles()
        observeAgentState()
    }

    private fun observeNeedles() {
        viewModelScope.launch {
            observeNeedlesUseCase().collect { needles ->
                _state.value = _state.value.copy(availableNeedles = needles.map { it.name })
            }
        }
    }

    private fun observeAgentState() {
        viewModelScope.launch {
            observeChatAgentStateUseCase().collect { agentState ->
                when (agentState) {
                    is AgentState.Processing -> {
                        _state.value =
                            _state.value.copy(
                                isProcessing = true,
                                processingToolCalls = agentState.toolCalls,
                                errorMessage = null,
                            )

                        // Add tool call messages to chat
                        agentState.toolCalls.forEach { toolName ->
                            // Check if this tool call message already exists
                            val toolCallExists =
                                _state.value.messages.any {
                                    it.role == MessageRole.TOOL && it.content == toolName
                                }

                            if (!toolCallExists) {
                                val toolMessage =
                                    Message(
                                        id = UUID.randomUUID().toString(),
                                        content = toolName,
                                        role = MessageRole.TOOL,
                                    )
                                _state.value =
                                    _state.value.copy(
                                        messages = _state.value.messages + toolMessage
                                    )
                            }
                        }
                    }

                    is AgentState.Error -> {
                        _state.value =
                            _state.value.copy(
                                isProcessing = false,
                                processingToolCalls = emptyList(),
                                errorMessage = agentState.message,
                            )
                    }

                    is AgentState.Completed -> {
                        _state.value =
                            _state.value.copy(
                                isProcessing = false,
                                processingToolCalls = emptyList(),
                            )
                    }

                    AgentState.Initializing,
                    is AgentState.Ready,
                    AgentState.Uninitialized -> {}
                }
            }
        }
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.UpdateInput -> {
                _state.value = _state.value.copy(currentInput = event.input)
            }

            HomeEvent.SendMessage -> sendMessage()
            HomeEvent.ClearChat -> clearChat()
            HomeEvent.OpenSettings -> navigationService.navigateTo(Destination.Settings)
            HomeEvent.OpenNeedles -> navigationService.navigateTo(Destination.Needles)
        }
    }

    private fun sendMessage() {
        val input = _state.value.currentInput.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isProcessing = true, errorMessage = null)

                // Add user message
                val userMessage =
                    Message(
                        id = UUID.randomUUID().toString(),
                        content = input,
                        role = MessageRole.USER,
                    )
                _state.value =
                    _state.value.copy(
                        messages = _state.value.messages + userMessage,
                        currentInput = "",
                    )

                // Run agent via use case
                val response =
                    runChatAgentUseCase(
                        userMessage = input,
                        onToolResult = { toolResult ->
                            toolResult.fold(
                                onSuccess = { result ->
                                    // Add tool result message
                                    val value = result.toDisplayString()
                                    val needleType = result.type
                                    val toolResultMessage =
                                        Message(
                                            id = UUID.randomUUID().toString(),
                                            content = value,
                                            role = MessageRole.TOOL_RESULT,
                                            contentType = MessageContentType.TEXT,
                                            imagePath = null,
                                        )

                                    _state.value =
                                        _state.value.copy(
                                            messages = _state.value.messages + toolResultMessage
                                        )
                                },
                                onFailure = { error -> Log.e(TAG, "Tool execution failed", error) },
                            )
                        },
                    )

                // Add assistant message only if it's not just the tool result
                // (avoid duplicate messages for image results)
                val lastToolResult =
                    _state.value.messages.lastOrNull { it.role == MessageRole.TOOL_RESULT }

                // Only add assistant message if response is different from tool result
                if (lastToolResult == null || lastToolResult.content != response) {
                    val assistantMessage =
                        Message(
                            id = UUID.randomUUID().toString(),
                            content = response,
                            role = MessageRole.ASSISTANT,
                        )

                    _state.value =
                        _state.value.copy(
                            messages = _state.value.messages + assistantMessage,
                            isProcessing = false,
                        )
                } else {
                    _state.value = _state.value.copy(isProcessing = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending message", e)
                _state.value =
                    _state.value.copy(isProcessing = false, errorMessage = "Error: ${e.message}")
                SnackbarUtil.showSnackbar("Error: ${e.message}")
            }
        }
    }

    private fun clearChat() {
        _state.value = _state.value.copy(messages = emptyList())
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
