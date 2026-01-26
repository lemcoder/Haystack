package io.github.lemcoder.core.model.llm

import kotlinx.serialization.Serializable

@Serializable
data class PromptExecutorConfig(
    val executorType: ExecutorType,
    val selectedModelName: String,
    val apiKey: String? = null,
)

@Serializable
enum class ExecutorType {
    OPEN_AI,
    OPEN_ROUTER,
    OLLAMA,
    LOCAL,
}
