package io.github.lemcoder.haystack.presentation.screen.needles

import io.github.lemcoder.haystack.core.model.needle.Needle

sealed interface NeedlesEvent {
    data object CreateNewNeedle : NeedlesEvent
    data class SelectNeedle(val needle: Needle) : NeedlesEvent
    data class DeleteNeedle(val needle: Needle) : NeedlesEvent
    data object ConfirmDelete : NeedlesEvent
    data object CancelDelete : NeedlesEvent
    data object DismissCreateDialog : NeedlesEvent
    data object NavigateBack : NeedlesEvent
}
