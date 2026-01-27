package io.github.lemcoder.haystack.presentation.screen.executorSettings

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.model.llm.ExecutorType
import io.github.lemcoder.core.model.llm.PromptExecutorConfig
import io.github.lemcoder.core.useCase.DeletePromptExecutorUseCase
import io.github.lemcoder.core.useCase.GetAllPromptExecutorsUseCase
import io.github.lemcoder.core.useCase.GetSelectedPromptExecutorUseCase
import io.github.lemcoder.core.useCase.SavePromptExecutorUseCase
import io.github.lemcoder.core.useCase.SelectPromptExecutorUseCase
import io.github.lemcoder.core.useCase.UpdatePromptExecutorUseCase
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                _state.value = _state.value.copy(isLoading = true)

                getAllPromptExecutorsUseCase().collect { executors ->
                    _state.value =
                        _state.value.copy(
                            executors = executors,
                            isLoading = false,
                            errorMessage = null,
                        )
                }
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = "Error loading executors: ${e.message}",
                    )
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
                // Silently fail, selectedExecutor will remain null
            }
        }
    }

    private fun selectExecutor(executorType: ExecutorType) {
        viewModelScope.launch {
            selectPromptExecutorUseCase(executorType)
                .fold(
                    onSuccess = {
                        SnackbarUtil.showSnackbar("Executor selected: ${executorType.name}")
                    },
                    onFailure = { error ->
                        SnackbarUtil.showSnackbar("Error selecting executor: ${error.message}")
                    },
                )
        }
    }

    private fun deleteExecutor(executorType: ExecutorType) {
        viewModelScope.launch {
            deletePromptExecutorUseCase(executorType)
                .fold(
                    onSuccess = {
                        SnackbarUtil.showSnackbar("Executor deleted: ${executorType.name}")
                    },
                    onFailure = { error ->
                        SnackbarUtil.showSnackbar("Error deleting executor: ${error.message}")
                    },
                )
        }
    }

    private fun saveExecutor(config: PromptExecutorConfig) {
        viewModelScope.launch {
            savePromptExecutorUseCase(config)
                .fold(
                    onSuccess = {
                        SnackbarUtil.showSnackbar("Executor saved: ${config.executorType.name}")
                    },
                    onFailure = { error ->
                        SnackbarUtil.showSnackbar("Error saving executor: ${error.message}")
                    },
                )
        }
    }

    private fun updateExecutor(config: PromptExecutorConfig) {
        viewModelScope.launch {
            updatePromptExecutorUseCase(config)
                .fold(
                    onSuccess = {
                        SnackbarUtil.showSnackbar("Executor updated: ${config.executorType.name}")
                    },
                    onFailure = { error ->
                        SnackbarUtil.showSnackbar("Error updating executor: ${error.message}")
                    },
                )
        }
    }
}
