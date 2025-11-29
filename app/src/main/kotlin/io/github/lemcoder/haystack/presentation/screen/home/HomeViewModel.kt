package io.github.lemcoder.haystack.presentation.screen.home

import android.util.Log
import androidx.lifecycle.viewModelScope
import io.github.lemcoder.haystack.core.data.NeedleRepository
import io.github.lemcoder.haystack.core.model.chat.Message
import io.github.lemcoder.haystack.core.model.chat.MessageRole
import io.github.lemcoder.haystack.core.service.AgentState
import io.github.lemcoder.haystack.core.service.ChatAgentService
import io.github.lemcoder.haystack.core.service.OnboardingService
import io.github.lemcoder.haystack.core.useCase.CreateSampleNeedlesUseCase
import io.github.lemcoder.haystack.core.useCase.RunChatAgentUseCase
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance,
    private val chatAgentService: ChatAgentService = ChatAgentService.Instance,
    private val runChatAgentUseCase: RunChatAgentUseCase = RunChatAgentUseCase(),
    private val navigationService: NavigationService = NavigationService.Instance,
    private val onboardingService: OnboardingService = OnboardingService.Instance,
    private val createSampleNeedlesUseCase: CreateSampleNeedlesUseCase = CreateSampleNeedlesUseCase()
) : MviViewModel<HomeState, HomeEvent>() {

    private val _state = MutableStateFlow(HomeState())
    override val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        checkAndPerformOnboarding()
        observeNeedles()
        observeAgentState()
        initializeAgent()
    }

    private fun checkAndPerformOnboarding() {
        viewModelScope.launch {
            onboardingService.onboardingState.collect { onboardingState ->
                if (!onboardingState.isOnboardingComplete) {
                    performOnboarding()
                }
            }
        }
    }

    private suspend fun performOnboarding() {
        try {
            Log.d(TAG, "Performing onboarding: Creating sample needles")
            createSampleNeedlesUseCase()
            onboardingService.markSampleNeedlesAdded()
            Log.d(TAG, "Onboarding completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during onboarding", e)
            SnackbarUtil.showSnackbar("Error creating sample needles: ${e.message ?: "Unknown error"}")
        }
    }

    private fun observeNeedles() {
        viewModelScope.launch {
            needleRepository.needlesFlow.collect { needles ->
                _state.value = _state.value.copy(
                    availableNeedles = needles.map { it.name }
                )
            }
        }
    }

    private fun observeAgentState() {
        viewModelScope.launch {
            chatAgentService.agentState.collect { agentState ->
                when (agentState) {
                    is AgentState.Processing -> {
                        _state.value = _state.value.copy(
                            isProcessing = true,
                            errorMessage = null
                        )
                    }

                    is AgentState.Error -> {
                        _state.value = _state.value.copy(
                            isProcessing = false,
                            errorMessage = agentState.message
                        )
                    }

                    else -> {
                        // Other states don't need special handling in UI yet
                    }
                }
            }
        }
    }

    private fun initializeAgent() {
        viewModelScope.launch {
            try {
                chatAgentService.initializeAgent()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize agent", e)
                _state.value = _state.value.copy(
                    errorMessage = "Failed to initialize AI: ${e.message}"
                )
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

                // Run agent via use case
                val response = runChatAgentUseCase(input)

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
        private const val TAG = "HomeViewModel"
    }
}
