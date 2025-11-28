package io.github.lemcoder.haystack.core.model.needle

import kotlinx.serialization.Serializable

@Serializable
data class Needle(
    val id: String,
    val name: String,
    val description: String,
    val pythonCode: String,
    val args: List<Arg>,
    val returnType: NeedleType,
    val dependencies: List<String> = emptyList(), // pip packages
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isLLMGenerated: Boolean = false,
) {
    @Serializable
    data class Arg(
        val name: String,
        val type: NeedleType,
        val description: String = "",
        val required: Boolean = true,
        val defaultValue: String? = null,
    )
}

@Serializable
sealed interface NeedleType {
    @Serializable
    data object String : NeedleType

    @Serializable
    data object Int : NeedleType

    @Serializable
    data object Float : NeedleType

    @Serializable
    data object Boolean : NeedleType

    @Serializable
    data object ByteArray : NeedleType

    @Serializable
    data object Any : NeedleType
}
