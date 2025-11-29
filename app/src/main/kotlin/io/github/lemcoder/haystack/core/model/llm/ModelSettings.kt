package io.github.lemcoder.haystack.core.model.llm

data class ModelSettings(
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val topK: Int? = null,
    val topP: Double? = null,
    val stopSequences: List<String> = emptyList(),
    val cactusToken: String? = null,
)