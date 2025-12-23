package io.github.lemcoder.haystack.presentation.screen.needleGenerator

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.service.needle.NeedleGeneratorService
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NeedleGeneratorViewModel(
    private val needleGeneratorService: NeedleGeneratorService = NeedleGeneratorService.Instance,
    private val navigationService: NavigationService = NavigationService.Instance,
) : MviViewModel<NeedleGeneratorState, NeedleGeneratorEvent>() {
    private val _state = MutableStateFlow(NeedleGeneratorState())
    override val state: StateFlow<NeedleGeneratorState> = _state.asStateFlow()

    override fun onEvent(event: NeedleGeneratorEvent) {
        when (event) {
            is NeedleGeneratorEvent.UpdateFunctionalityDescription -> {
                _state.value =
                    _state.value.copy(functionalityDescription = event.value, errorMessage = null)
            }

            NeedleGeneratorEvent.GenerateNeedle -> generateNeedle()
            NeedleGeneratorEvent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }

            NeedleGeneratorEvent.NavigateBack -> {
                navigationService.navigateBack()
            }
        }
    }

    private fun generateNeedle() {
        val description = _state.value.functionalityDescription.trim()

        if (description.isBlank()) {
            _state.value =
                _state.value.copy(
                    errorMessage = "Please provide a description of the functionality"
                )
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isGenerating = true, errorMessage = null)

                val result = needleGeneratorService.generateNeedle(description)

                result.fold(
                    onSuccess = { needle ->
                        _state.value =
                            _state.value.copy(
                                isGenerating = false,
                                generatedNeedleName = needle.name,
                                functionalityDescription = "",
                            )
                        SnackbarUtil.showSnackbar("Needle '${needle.name}' generated successfully!")

                        // Navigate back after a short delay
                        kotlinx.coroutines.delay(1500)
                        navigationService.navigateBack()
                    },
                    onFailure = { error ->
                        _state.value =
                            _state.value.copy(
                                isGenerating = false,
                                errorMessage = "Failed to generate needle: ${error.message}",
                            )
                        SnackbarUtil.showSnackbar("Failed to generate needle: ${error.message}")
                    },
                )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isGenerating = false,
                        errorMessage = "Unexpected error: ${e.message}",
                    )
                SnackbarUtil.showSnackbar("Unexpected error: ${e.message}")
            }
        }
    }
}
