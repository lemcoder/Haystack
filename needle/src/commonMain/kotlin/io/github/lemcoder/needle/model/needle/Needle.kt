package io.github.lemcoder.needle.model.needle

import kotlin.time.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Needle(
    val id: String,
    val name: String,
    val description: String,
    val code: String,
    val args: List<Arg>,
    val returnType: Arg.Type,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
) {
    @Serializable
    data class Arg(
        val name: String,
        val type: Type,
        val description: String = "",
        val required: Boolean = true,
        val defaultValue: String? = null,
    ) {
        @Serializable
        sealed interface Type {
            @Serializable data object String : Type

            @Serializable data object Int : Type

            @Serializable data object Float : Type

            @Serializable data object Boolean : Type

            @Serializable data object Image : Type

            @Serializable data object Any : Type
        }
    }
}
