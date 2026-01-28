package io.github.lemcoder.haystack.util

import io.github.lemcoder.core.model.llm.ExecutorType

/**
 * Returns a human-readable display name for the executor type
 */
fun ExecutorType.displayName(): String {
    return when (this) {
        is ExecutorType.OpenAI -> "OpenAI"
        is ExecutorType.OpenRouter -> "OpenRouter"
        is ExecutorType.Ollama -> "Ollama"
        is ExecutorType.Local -> "Local"
    }
}

/**
 * Gets the API key if the executor type supports it
 */
fun ExecutorType.apiKey(): String? {
    return when (this) {
        is ExecutorType.OpenAI -> this.apiKey
        is ExecutorType.OpenRouter -> this.apiKey
        else -> null
    }
}

/**
 * Gets the base URL if the executor type supports it
 */
fun ExecutorType.baseUrl(): String? {
    return when (this) {
        is ExecutorType.Ollama -> this.baseUrl
        else -> null
    }
}

/**
 * Returns all available executor type variants for selection
 */
object ExecutorTypeVariants {
    val OpenAI = ExecutorType.OpenAI(apiKey = "")
    val OpenRouter = ExecutorType.OpenRouter(apiKey = "")
    val Ollama = ExecutorType.Ollama(baseUrl = "http://localhost:11434")
    val Local = ExecutorType.Local

    fun all(): List<ExecutorType> = listOf(OpenAI, OpenRouter, Ollama, Local)
}

/**
 * Gets the type class without parameters for comparison
 */
fun ExecutorType.typeClass(): String {
    return when (this) {
        is ExecutorType.OpenAI -> "OpenAI"
        is ExecutorType.OpenRouter -> "OpenRouter"
        is ExecutorType.Ollama -> "Ollama"
        is ExecutorType.Local -> "Local"
    }
}

/**
 * Checks if two ExecutorType instances are of the same type class
 */
fun ExecutorType.isSameTypeAs(other: ExecutorType): Boolean {
    return this.typeClass() == other.typeClass()
}
