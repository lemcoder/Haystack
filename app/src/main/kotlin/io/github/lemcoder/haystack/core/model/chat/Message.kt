package io.github.lemcoder.haystack.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCalls: List<ToolCall> = emptyList()
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

@Serializable
data class ToolCall(
    val toolName: String,
    val arguments: Map<String, String>,
    val result: String? = null
)
