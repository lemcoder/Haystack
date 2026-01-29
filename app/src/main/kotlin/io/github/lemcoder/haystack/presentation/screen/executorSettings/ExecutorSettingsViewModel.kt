package io.github.lemcoder.haystack.presentation.screen.executorSettings

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.model.llm.ExecutorType
import io.github.lemcoder.core.model.llm.PromptExecutorConfig
import io.github.lemcoder.core.useCase.executor.DeletePromptExecutorUseCase
import io.github.lemcoder.core.useCase.executor.GetAllPromptExecutorsUseCase
import io.github.lemcoder.core.useCase.executor.GetSelectedPromptExecutorUseCase
import io.github.lemcoder.core.useCase.executor.SavePromptExecutorUseCase
import io.github.lemcoder.core.useCase.executor.SelectPromptExecutorUseCase
import io.github.lemcoder.core.useCase.executor.UpdatePromptExecutorUseCase
import io.github.lemcoder.haystack.designSystem.component.toast.Toast
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.displayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExecutorSettingsViewModel(
    private val getAllPromptExecutorsUseCase: GetAllPromptExecutorsUseCase =
        GetAllPromptExecutorsUseCase.create(),
    private val getSelectedPromptExecutorUseCase: GetSelectedPromptExecutorUseCase =
        GetSelectedPromptExecutorUseCase.create(),
    private val savePromptExecutorUseCase: SavePromptExecutorUseCase =
        SavePromptExecutorUseCase.create(),
    private val updatePromptExecutorUseCase: UpdatePromptExecutorUseCase =
        UpdatePromptExecutorUseCase.create(),
    private val deletePromptExecutorUseCase: DeletePromptExecutorUseCase =
        DeletePromptExecutorUseCase.create(),
    private val selectPromptExecutorUseCase: SelectPromptExecutorUseCase =
        SelectPromptExecutorUseCase.create(),
    private val navigationService: NavigationService = NavigationService.Instance,
) : MviViewModel<ExecutorSettingsState, ExecutorSettingsEvent>() {
    private val _state = MutableStateFlow(ExecutorSettingsState())
    override val state: StateFlow<ExecutorSettingsState> = _state.asStateFlow()

    init {
        loadExecutors()
        loadSelectedExecutor()
    }

    override fun onEvent(event: ExecutorSettingsEvent) {
        when (event) {
            ExecutorSettingsEvent.NavigateBack -> {
                navigationService.navigateBack()
            }

            ExecutorSettingsEvent.NavigateToAddExecutor -> {
                navigationService.navigateTo(Destination.ExecutorEdit(executorType = null))
            }

            is ExecutorSettingsEvent.NavigateToEditExecutor -> {
                navigationService.navigateTo(
                    Destination.ExecutorEdit(executorType = event.executorType)
                )
            }

            is ExecutorSettingsEvent.SelectExecutor -> {
                selectExecutor(event.executorType)
            }

            is ExecutorSettingsEvent.DeleteExecutor -> {
                deleteExecutor(event.executorType)
            }

            is ExecutorSettingsEvent.SaveExecutor -> {
                saveExecutor(event.config)
            }

            is ExecutorSettingsEvent.UpdateExecutor -> {
                updateExecutor(event.config)
            }
        }
    }

    private fun loadExecutors() {
        viewModelScope.launch {
            try {
                _state.update { state -> state.copy(isLoading = true) }

                getAllPromptExecutorsUseCase().collect { executors ->
                    _state.update { state ->
                        state.copy(executors = executors, isLoading = false, errorMessage = null)
                    }
                }
            } catch (e: Exception) {
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "Error loading executors: ${e.message}",
                    )
                }
            }
        }
    }

    private fun loadSelectedExecutor() {
        viewModelScope.launch {
            try {
                getSelectedPromptExecutorUseCase().collect { executor ->
                    _state.value = _state.value.copy(selectedExecutor = executor)
                }
            } catch (e: Exception) {
                // Silently fail if no executor is selected yet or repository is not initialized.
                // The selectedExecutor will remain null, and no executor will be highlighted.
            }
        }
    }

    private fun selectExecutor(executorType: ExecutorType) {
        viewModelScope.launch {
            selectPromptExecutorUseCase(executorType)
                .fold(
                    onSuccess = { Toast.show("Executor selected: ${executorType.displayName()}") },
                    onFailure = { error ->
                        Toast.show("Error selecting executor: ${error.message}")
                    },
                )
        }
    }

    private fun deleteExecutor(executorType: ExecutorType) {
        viewModelScope.launch {
            deletePromptExecutorUseCase(executorType)
                .fold(
                    onSuccess = { Toast.show("Executor deleted: ${executorType.displayName()}") },
                    onFailure = { error -> Toast.show("Error deleting executor: ${error.message}") },
                )
        }
    }

    private fun saveExecutor(config: PromptExecutorConfig) {
        viewModelScope.launch {
            savePromptExecutorUseCase(config)
                .fold(
                    onSuccess = {
                        Toast.show("Executor saved: ${config.executorType.displayName()}")
                    },
                    onFailure = { error -> Toast.show("Error saving executor: ${error.message}") },
                )
        }
    }

    private fun updateExecutor(config: PromptExecutorConfig) {
        viewModelScope.launch {
            updatePromptExecutorUseCase(config)
                .fold(
                    onSuccess = {
                        Toast.show("Executor updated: ${config.executorType.displayName()}")
                    },
                    onFailure = { error -> Toast.show("Error updating executor: ${error.message}") },
                )
        }
    }
}
