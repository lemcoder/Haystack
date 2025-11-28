package io.github.lemcoder.haystack.presentation.screen.settings

sealed interface SettingsEvent {
    data class UpdateTemperature(val value: String) : SettingsEvent
    data class UpdateMaxTokens(val value: String) : SettingsEvent
    data class UpdateTopK(val value: String) : SettingsEvent
    data class UpdateTopP(val value: String) : SettingsEvent
    data class UpdateStopSequences(val value: String) : SettingsEvent
    data class UpdateCactusToken(val value: String) : SettingsEvent
    data class UpdateInferenceMode(val mode: String) : SettingsEvent
    data class UpdateInternetAccess(val enabled: Boolean) : SettingsEvent
    data object SaveSettings : SettingsEvent
}
