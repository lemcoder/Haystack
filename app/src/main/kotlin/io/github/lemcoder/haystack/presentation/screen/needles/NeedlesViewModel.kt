package io.github.lemcoder.haystack.presentation.screen.needles

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.useCase.needle.GetAllNeedlesUseCase
import io.github.lemcoder.core.useCase.needle.ToggleNeedleVisibilityUseCase
import io.github.lemcoder.haystack.designSystem.component.toast.Toast
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NeedlesViewModel(
    private val getAllNeedlesUseCase: GetAllNeedlesUseCase = GetAllNeedlesUseCase.create(),
    private val toggleNeedleVisibilityUseCase: ToggleNeedleVisibilityUseCase =
        ToggleNeedleVisibilityUseCase.create(),
    private val needleRepository: NeedleRepository = NeedleRepository.Instance,
    private val navigationService: NavigationService = NavigationService.Instance,
) : MviViewModel<NeedlesState, NeedlesEvent>() {
    private val _state = MutableStateFlow(NeedlesState())
    override val state: StateFlow<NeedlesState> = _state.asStateFlow()

    init {
        loadNeedles()
        loadHiddenNeedleIds()
    }

    override fun onEvent(event: NeedlesEvent) {
        when (event) {
            is NeedlesEvent.SelectNeedle -> {
                navigationService.navigateTo(Destination.NeedleDetail(event.needle.id))
            }

            is NeedlesEvent.ToggleNeedleVisibility -> {
                toggleVisibility(event.needle)
            }

            NeedlesEvent.DismissCreateDialog -> {
                _state.update { it.copy(showCreateDialog = false) }
            }

            NeedlesEvent.NavigateBack -> {
                navigationService.navigateBack()
            }
        }
    }

    private fun loadNeedles() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                getAllNeedlesUseCase().collect { needles ->
                    _state.update {
                        it.copy(needles = needles, isLoading = false, errorMessage = null)
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error loading needles: ${e.message}",
                    )
                }
            }
        }
    }

    private fun loadHiddenNeedleIds() {
        viewModelScope.launch {
            try {
                needleRepository.hiddenNeedleIdsFlow.collect { hiddenIds ->
                    _state.update { it.copy(hiddenNeedleIds = hiddenIds) }
                }
            } catch (e: Exception) {
                // Silently fail, hiddenNeedleIds will remain empty
            }
        }
    }

    private fun toggleVisibility(needle: Needle) {
        viewModelScope.launch {
            try {
                val wasHidden = needleRepository.isNeedleHidden(needle.id)
                toggleNeedleVisibilityUseCase(needle.id)
                    .fold(
                        onSuccess = {
                            val message =
                                if (wasHidden) {
                                    "Needle shown: ${needle.name}"
                                } else {
                                    "Needle hidden: ${needle.name}"
                                }
                            Toast.show(message)
                        },
                        onFailure = { error ->
                            Toast.show("Error toggling visibility: ${error.message}")
                        },
                    )
            } catch (e: Exception) {
                Toast.show("Error toggling visibility: ${e.message}")
            }
        }
    }
}
