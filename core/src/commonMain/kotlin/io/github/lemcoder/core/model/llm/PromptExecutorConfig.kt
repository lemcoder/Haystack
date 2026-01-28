package io.github.lemcoder.core.model.llm

import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import kotlinx.serialization.Serializable

@Serializable
data class PromptExecutorConfig(val executorType: ExecutorType, val selectedModelName: String)

@Serializable
sealed interface ExecutorType {
    @Serializable data class OpenAI(val apiKey: String, val baseUrl: String? = null) : ExecutorType

    @Serializable data class OpenRouter(val apiKey: String) : ExecutorType

    @Serializable data class Ollama(val baseUrl: String) : ExecutorType

    @Serializable data object Local : ExecutorType
}

internal fun PromptExecutorConfig.toPromptExecutor(): PromptExecutor {
    return when (val type = this.executorType) {
        is ExecutorType.OpenAI -> {
            SingleLLMPromptExecutor(
                OpenAILLMClient(
                    apiKey = type.apiKey,
                    settings =
                        type.baseUrl?.let { OpenAIClientSettings(baseUrl = it) }
                            ?: OpenAIClientSettings(),
                )
            )
        }

        is ExecutorType.OpenRouter -> simpleOpenRouterExecutor(type.apiKey)
        is ExecutorType.Ollama -> simpleOllamaAIExecutor(type.baseUrl)
        is ExecutorType.Local -> TODO()
    }
}
