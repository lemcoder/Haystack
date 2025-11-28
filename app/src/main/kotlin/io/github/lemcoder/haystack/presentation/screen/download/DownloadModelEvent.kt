package io.github.lemcoder.haystack.presentation.screen.download

sealed interface DownloadModelEvent {
    data object StartDownload : DownloadModelEvent
}