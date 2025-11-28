package io.github.lemcoder.haystack.presentation.screen.settings

data class SettingsState(
    val temperature: String = "",
    val maxTokens: String = "",
    val topK: String = "",
    val topP: String = "",
    val stopSequences: String = "",
    val cactusToken: String = "",
    val inferenceMode: String = "LOCAL",
    val allowInternetAccess: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)
