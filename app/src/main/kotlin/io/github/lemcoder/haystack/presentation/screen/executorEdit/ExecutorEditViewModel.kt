package io.github.lemcoder.haystack.presentation.screen.executorEdit

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.model.llm.ExecutorType
import io.github.lemcoder.core.model.llm.PromptExecutorConfig
import io.github.lemcoder.core.useCase.GetAllPromptExecutorsUseCase
import io.github.lemcoder.core.useCase.SavePromptExecutorUseCase
import io.github.lemcoder.core.useCase.UpdatePromptExecutorUseCase
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ExecutorEditViewModel(
    private val executorType: ExecutorType? = null,
    private val getAllPromptExecutorsUseCase: GetAllPromptExecutorsUseCase =
        GetAllPromptExecutorsUseCase.create(),
    private val savePromptExecutorUseCase: SavePromptExecutorUseCase =
        SavePromptExecutorUseCase.create(),
    private val updatePromptExecutorUseCase: UpdatePromptExecutorUseCase =
        UpdatePromptExecutorUseCase.create(),
    private val navigationService: NavigationService = NavigationService.Instance,
) : MviViewModel<ExecutorEditState, ExecutorEditEvent>() {
    private val _state = MutableStateFlow(ExecutorEditState())
    override val state: StateFlow<ExecutorEditState> = _state.asStateFlow()

    init {
        if (executorType != null) {
            loadExecutor(executorType)
        }
    }

    override fun onEvent(event: ExecutorEditEvent) {
        when (event) {
            ExecutorEditEvent.NavigateBack -> {
                navigationService.navigateBack()
            }

            is ExecutorEditEvent.UpdateExecutorType -> {
                _state.value = _state.value.copy(executorType = event.executorType)
            }

            is ExecutorEditEvent.UpdateModelName -> {
                _state.value = _state.value.copy(selectedModelName = event.modelName)
            }

            is ExecutorEditEvent.UpdateApiKey -> {
                _state.value = _state.value.copy(apiKey = event.apiKey)
            }

            ExecutorEditEvent.SaveExecutor -> {
                saveExecutor()
            }
        }
    }

    private fun loadExecutor(executorType: ExecutorType) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val executors = getAllPromptExecutorsUseCase().first()
                val executor = executors.firstOrNull { it.executorType == executorType }

                if (executor != null) {
                    _state.value =
                        _state.value.copy(
                            executorType = executor.executorType,
                            selectedModelName = executor.selectedModelName,
                            apiKey = executor.apiKey ?: "",
                            isEditMode = true,
                            isLoading = false,
                        )
                } else {
                    _state.value =
                        _state.value.copy(isLoading = false, errorMessage = "Executor not found")
                }
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = "Error loading executor: ${e.message}",
                    )
            }
        }
    }

    private fun saveExecutor() {
        val currentState = _state.value

        // Validation
        if (currentState.executorType == null) {
            SnackbarUtil.showSnackbar("Please select an executor type")
            return
        }

        if (currentState.selectedModelName.isBlank()) {
            SnackbarUtil.showSnackbar("Please enter a model name")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val config =
                    PromptExecutorConfig(
                        executorType = currentState.executorType,
                        selectedModelName = currentState.selectedModelName,
                        apiKey = currentState.apiKey.takeIf { it.isNotBlank() },
                    )

                val result =
                    if (currentState.isEditMode) {
                        updatePromptExecutorUseCase(config)
                    } else {
                        savePromptExecutorUseCase(config)
                    }

                result.fold(
                    onSuccess = {
                        val message =
                            if (currentState.isEditMode) "Executor updated successfully"
                            else "Executor saved successfully"
                        SnackbarUtil.showSnackbar(message)
                        navigationService.navigateBack()
                    },
                    onFailure = { error ->
                        _state.value =
                            _state.value.copy(
                                isLoading = false,
                                errorMessage = error.message ?: "Unknown error",
                            )
                        SnackbarUtil.showSnackbar("Error: ${error.message}")
                    },
                )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = "Error saving executor: ${e.message}",
                    )
                SnackbarUtil.showSnackbar("Error: ${e.message}")
            }
        }
    }
}
