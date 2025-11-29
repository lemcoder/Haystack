package io.github.lemcoder.haystack.presentation.screen.home

import io.github.lemcoder.haystack.core.model.chat.Message

data class HomeState(
    // Chat state
    val messages: List<Message> = emptyList(),
    val currentInput: String = "",
    val isProcessing: Boolean = false,
    val availableNeedles: List<String> = emptyList(),
    val errorMessage: String? = null
)
