package io.github.lemcoder.haystack.presentation.screen.needles

import io.github.lemcoder.core.model.needle.Needle

sealed interface NeedlesEvent {
    data class SelectNeedle(val needle: Needle) : NeedlesEvent

    data class ToggleNeedleVisibility(val needle: Needle) : NeedlesEvent

    data object DismissCreateDialog : NeedlesEvent

    data object NavigateBack : NeedlesEvent
}
