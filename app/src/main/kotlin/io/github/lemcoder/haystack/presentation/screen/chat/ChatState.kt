package io.github.lemcoder.haystack.presentation.screen.chat

import io.github.lemcoder.haystack.core.model.chat.Message

data class ChatState(
    val messages: List<Message> = emptyList(),
    val currentInput: String = "",
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val availableNeedles: List<String> = emptyList()
)
