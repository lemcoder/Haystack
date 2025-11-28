package io.github.lemcoder.haystack.presentation.screen.download

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.haystack.core.useCase.CreateSampleNeedlesUseCase
import io.github.lemcoder.haystack.core.useCase.DownloadLocalModelUseCase
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadModelViewModel(
    private val navigationService: NavigationService = NavigationService.Instance,
    private val downloadLocalModelUseCase: DownloadLocalModelUseCase = DownloadLocalModelUseCase.create(),
    private val createSampleNeedlesUseCase: CreateSampleNeedlesUseCase = CreateSampleNeedlesUseCase()
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
                downloadLocalModelUseCase().collect {
                    _state.value = _state.value.copy(
                        isDownloading = true,
                    )
                }
                _state.value = _state.value.copy(
                    isDownloading = false,
                )

                // Initialize sample needles after model download (onboarding)
                createSampleNeedlesUseCase()

                navigationService.navigateTo(Destination.Home)
            } catch (ex: Exception) {
                SnackbarUtil.showSnackbar(
                    message = "Error downloading model: ${ex.message ?: "Unknown error"}"
                )
            }

        }
    }
}