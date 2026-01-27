package io.github.lemcoder.haystack.presentation.screen.settings

sealed interface SettingsEvent {
    data object NavigateBack : SettingsEvent

    data object NavigateToExecutorSettings : SettingsEvent
}
