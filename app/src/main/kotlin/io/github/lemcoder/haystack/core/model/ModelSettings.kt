package io.github.lemcoder.haystack.core.model

import com.cactus.InferenceMode

data class ModelSettings(
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val topK: Int? = null,
    val topP: Double? = null,
    val stopSequences: List<String> = emptyList(),
    val cactusToken: String? = null,
    val inferenceMode: InferenceMode = InferenceMode.LOCAL,
    val allowInternetAccess: Boolean = false
)