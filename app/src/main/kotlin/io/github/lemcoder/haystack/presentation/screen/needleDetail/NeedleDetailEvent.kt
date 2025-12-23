package io.github.lemcoder.haystack.presentation.screen.needleDetail

sealed interface NeedleDetailEvent {
  data object NavigateBack : NeedleDetailEvent

  data object ExecuteNeedle : NeedleDetailEvent

  data object DismissResult : NeedleDetailEvent

  data object ShowArgumentsDialog : NeedleDetailEvent

  data object DismissArgumentsDialog : NeedleDetailEvent

  data class UpdateArgument(val argName: String, val value: String) : NeedleDetailEvent

  data object ConfirmAndExecute : NeedleDetailEvent
}
