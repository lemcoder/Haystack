package io.github.lemcoder.core.needle

import io.github.lemcoder.core.model.needle.Needle

/**
 * Type-safe representation of needle parameters. Each parameter holds its value with compile-time
 * type safety, eliminating the need for runtime type casting with Any.
 */
sealed class NeedleParameter {
    abstract val name: String
    abstract val type: Needle.Arg.Type

    data class StringParam(override val name: String, val value: String) : NeedleParameter() {
        override val type = Needle.Arg.Type.String
    }

    data class IntParam(override val name: String, val value: Int) : NeedleParameter() {
        override val type = Needle.Arg.Type.Int
    }

    data class FloatParam(override val name: String, val value: Float) : NeedleParameter() {
        override val type = Needle.Arg.Type.Float
    }

    data class BooleanParam(override val name: String, val value: Boolean) : NeedleParameter() {
        override val type = Needle.Arg.Type.Boolean
    }

    /** Get the underlying value as Any for compatibility with legacy code */
    fun getValue(): Any =
        when (this) {
            is StringParam -> value
            is IntParam -> value
            is FloatParam -> value
            is BooleanParam -> value
        }
}

/** Helper to convert a list of parameters to a map for backward compatibility */
fun List<NeedleParameter>.toParamMap(): Map<String, Any> = associate { it.name to it.getValue() }
