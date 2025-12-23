package io.github.lemcoder.haystack.presentation.screen.home

sealed interface HomeEvent {
  data class UpdateInput(val input: String) : HomeEvent

  data object SendMessage : HomeEvent

  data object ClearChat : HomeEvent

  data object OpenSettings : HomeEvent

  data object OpenNeedles : HomeEvent
}
