package io.github.lemcoder.core.model.needle

import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class Needle(
    val id: String,
    val name: String,
    val description: String,
    val pythonCode: String,
    val args: List<Arg>,
    val returnType: NeedleType,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
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
    data object Image : NeedleType

    @Serializable
    data object Any : NeedleType
}
