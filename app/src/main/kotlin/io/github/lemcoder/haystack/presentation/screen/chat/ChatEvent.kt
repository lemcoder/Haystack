package io.github.lemcoder.haystack.presentation.screen.chat

sealed interface ChatEvent {
    data class UpdateInput(val input: String) : ChatEvent
    data object SendMessage : ChatEvent
    data object ClearChat : ChatEvent
    data object OpenSettings : ChatEvent
    data object OpenNeedles : ChatEvent
}
