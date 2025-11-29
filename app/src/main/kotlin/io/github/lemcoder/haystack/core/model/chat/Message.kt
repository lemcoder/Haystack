package io.github.lemcoder.haystack.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis(),
    val toolCalls: List<ToolCall> = emptyList(),
    val contentType: MessageContentType = MessageContentType.TEXT,
    val imagePath: String? = null // File path to image
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL,
    TOOL_RESULT
}

@Serializable
enum class MessageContentType {
    TEXT,
    IMAGE,
    MIXED // For messages with both text and image
}

@Serializable
data class ToolCall(
    val toolName: String,
    val arguments: Map<String, String>,
    val result: String? = null
)
