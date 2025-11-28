package io.github.lemcoder.haystack.navigation

sealed interface Destination {
    data object DownloadModel : Destination

    data object Home : Destination
    data object Needles : Destination
    data class NeedleDetail(val needleId: String) : Destination

    data object Settings: Destination
}