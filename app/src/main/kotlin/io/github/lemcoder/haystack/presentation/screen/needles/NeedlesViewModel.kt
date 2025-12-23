package io.github.lemcoder.haystack.presentation.screen.needles

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.haystack.core.useCase.GetAllNeedlesUseCase
import io.github.lemcoder.haystack.core.useCase.ToggleNeedleVisibilityUseCase
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
  private val toggleNeedleVisibilityUseCase: ToggleNeedleVisibilityUseCase =
    ToggleNeedleVisibilityUseCase(),
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
      NeedlesEvent.CreateNewNeedle -> {
        navigationService.navigateTo(Destination.NeedleGenerator)
      }

      is NeedlesEvent.SelectNeedle -> {
        navigationService.navigateTo(Destination.NeedleDetail(event.needle.id))
      }

      is NeedlesEvent.ToggleNeedleVisibility -> {
        toggleVisibility(event.needle)
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
          _state.value =
            _state.value.copy(needles = needles, isLoading = false, errorMessage = null)
        }
      } catch (e: Exception) {
        _state.value =
          _state.value.copy(isLoading = false, errorMessage = "Error loading needles: ${e.message}")
      }
    }
  }

  private fun loadHiddenNeedleIds() {
    viewModelScope.launch {
      try {
        needleRepository.hiddenNeedleIdsFlow.collect { hiddenIds ->
          _state.value = _state.value.copy(hiddenNeedleIds = hiddenIds)
        }
      } catch (e: Exception) {
        // Silently fail, hiddenNeedleIds will remain empty
      }
    }
  }

  private fun toggleVisibility(needle: io.github.lemcoder.core.model.needle.Needle) {
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
              SnackbarUtil.showSnackbar(message)
            },
            onFailure = { error ->
              SnackbarUtil.showSnackbar("Error toggling visibility: ${error.message}")
            },
          )
      } catch (e: Exception) {
        SnackbarUtil.showSnackbar("Error toggling visibility: ${e.message}")
      }
    }
  }
}
