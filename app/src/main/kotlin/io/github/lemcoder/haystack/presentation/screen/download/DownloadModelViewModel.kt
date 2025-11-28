package io.github.lemcoder.haystack.presentation.screen.download

import androidx.lifecycle.viewModelScope
import io.github.lemcoder.haystack.common.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadModelViewModel : MviViewModel<DownloadModelState, DownloadModelEvent>() {
    private val _state = MutableStateFlow(DownloadModelState())
    override val state: StateFlow<DownloadModelState> = _state.asStateFlow()

    override fun onEvent(event: DownloadModelEvent) {
        when (event) {
            DownloadModelEvent.StartDownload -> startDownload()
        }
    }

    private fun startDownload() {
        viewModelScope.launch {
            // Implement download logic here
        }
    }
}