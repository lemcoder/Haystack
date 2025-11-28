package io.github.lemcoder.haystack.presentation.screen.needles

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.haystack.core.useCase.CreateSampleNeedlesUseCase
import io.github.lemcoder.haystack.core.useCase.DeleteNeedleUseCase
import io.github.lemcoder.haystack.core.useCase.GetAllNeedlesUseCase
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NeedlesViewModel(
    private val getAllNeedlesUseCase: GetAllNeedlesUseCase = GetAllNeedlesUseCase(),
    private val deleteNeedleUseCase: DeleteNeedleUseCase = DeleteNeedleUseCase(),
    private val createSampleNeedlesUseCase: CreateSampleNeedlesUseCase = CreateSampleNeedlesUseCase(),
    private val navigationService: NavigationService = NavigationService.Instance
) : MviViewModel<NeedlesState, NeedlesEvent>() {
    private val _state = MutableStateFlow(NeedlesState())
    override val state: StateFlow<NeedlesState> = _state.asStateFlow()

    init {
        initializeSamples()
        loadNeedles()
    }

    private fun initializeSamples() {
        viewModelScope.launch {
            try {
                createSampleNeedlesUseCase()
            } catch (e: Exception) {
                // Silently fail - samples are optional
            }
        }
    }

    override fun onEvent(event: NeedlesEvent) {
        when (event) {
            NeedlesEvent.CreateNewNeedle -> {
                _state.value = _state.value.copy(showCreateDialog = true)
            }

            is NeedlesEvent.SelectNeedle -> {
                navigationService.navigateTo(Destination.NeedleDetail(event.needle.id))
            }

            is NeedlesEvent.DeleteNeedle -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = true,
                    needleToDelete = event.needle
                )
            }

            NeedlesEvent.ConfirmDelete -> {
                confirmDelete()
            }

            NeedlesEvent.CancelDelete -> {
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    needleToDelete = null
                )
            }

            NeedlesEvent.DismissCreateDialog -> {
                _state.value = _state.value.copy(showCreateDialog = false)
            }

            NeedlesEvent.NavigateBack -> {
                navigationService.navigateBack()
            }
        }
    }

    private fun loadNeedles() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                getAllNeedlesUseCase().collect { needles ->
                    _state.value = _state.value.copy(
                        needles = needles,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading needles: ${e.message}"
                )
            }
        }
    }

    private fun confirmDelete() {
        val needle = _state.value.needleToDelete ?: return

        viewModelScope.launch {
            try {
                deleteNeedleUseCase(needle.id).fold(
                    onSuccess = {
                        SnackbarUtil.showSnackbar("Needle deleted: ${needle.name}")
                        _state.value = _state.value.copy(
                            showDeleteDialog = false,
                            needleToDelete = null
                        )
                    },
                    onFailure = { error ->
                        SnackbarUtil.showSnackbar("Error deleting needle: ${error.message}")
                        _state.value = _state.value.copy(
                            showDeleteDialog = false,
                            needleToDelete = null
                        )
                    }
                )
            } catch (e: Exception) {
                SnackbarUtil.showSnackbar("Error deleting needle: ${e.message}")
                _state.value = _state.value.copy(
                    showDeleteDialog = false,
                    needleToDelete = null
                )
            }
        }
    }
}
