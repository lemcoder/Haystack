package io.github.lemcoder.haystack.presentation.screen.download

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.useCase.CreateSampleNeedlesUseCase
import io.github.lemcoder.core.useCase.DownloadLocalModelUseCase
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadModelViewModel(
    private val navigationService: NavigationService = NavigationService.Instance,
    private val downloadLocalModelUseCase: DownloadLocalModelUseCase =
        DownloadLocalModelUseCase.create(),
    private val createSampleNeedlesUseCase: CreateSampleNeedlesUseCase =
        CreateSampleNeedlesUseCase.create(),
) : MviViewModel<DownloadModelState, DownloadModelEvent>() {
    private val _state = MutableStateFlow(DownloadModelState())
    override val state: StateFlow<DownloadModelState> = _state.asStateFlow()

    override fun onEvent(event: DownloadModelEvent) {
        when (event) {
            DownloadModelEvent.StartDownload -> startDownload()
        }
    }

    private fun startDownload() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isDownloading = true, errorMessage = null)

                downloadLocalModelUseCase().collect { progress ->
                    // Progress updates are received here but we don't need to show them
                    // Just keep the downloading state active
                }

                _state.value = _state.value.copy(isDownloading = false)

                // Initialize sample needles after model download (onboarding)
                createSampleNeedlesUseCase()

                navigationService.navigateTo(Destination.Home)
            } catch (ex: Exception) {
                _state.value =
                    _state.value.copy(
                        isDownloading = false,
                        errorMessage = "Error downloading model: ${ex.message ?: "Unknown error"}",
                    )
            }
        }
    }
}
