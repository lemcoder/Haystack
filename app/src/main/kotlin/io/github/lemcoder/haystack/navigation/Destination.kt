package io.github.lemcoder.haystack.navigation

sealed interface Destination {
    data object DownloadModel : Destination

    data object Home : Destination
    data object Needles : Destination

    data object Settings: Destination
}