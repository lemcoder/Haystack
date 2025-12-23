package io.github.lemcoder.core.model.chat

import kotlin.time.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val toolCalls: List<ToolCall> = emptyList(),
    val contentType: MessageContentType = MessageContentType.TEXT,
    val imagePath: String? = null, // File path to image
)

@kotlinx.serialization.Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL,
    TOOL_RESULT,
}

@Serializable
enum class MessageContentType {
    TEXT,
    IMAGE,
    MIXED, // For messages with both text and image
}

@Serializable
data class ToolCall(
    val toolName: String,
    val arguments: Map<String, String>,
    val result: String? = null,
)
