package io.github.lemcoder.haystack.navigation

import io.github.lemcoder.core.model.llm.ExecutorType

sealed interface Destination {
    data object Home : Destination // Main chat interface with Needles

    data object Needles : Destination

    data class NeedleDetail(val needleId: String) : Destination

    data object Settings : Destination

    data object ExecutorSettings : Destination

    data class ExecutorEdit(val executorType: ExecutorType?) : Destination
}
