package io.github.lemcoder.core.model.llm

import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import kotlinx.serialization.Serializable

@Serializable
data class PromptExecutorConfig(val executorType: ExecutorType, val selectedModelName: String)

@Serializable
sealed interface ExecutorType {
    data class OpenAI(val apiKey: String) : ExecutorType

    data class OpenRouter(val apiKey: String) : ExecutorType

    data class Ollama(val baseUrl: String) : ExecutorType

    data object Local : ExecutorType
}

internal fun PromptExecutorConfig.toPromptExecutor(): PromptExecutor {
    return when (val type = this.executorType) {
        is ExecutorType.OpenAI -> simpleOpenAIExecutor(type.apiKey)
        is ExecutorType.OpenRouter -> simpleOpenRouterExecutor(type.apiKey)
        is ExecutorType.Ollama -> simpleOllamaAIExecutor(type.baseUrl)
        is ExecutorType.Local -> TODO()
    }
}
