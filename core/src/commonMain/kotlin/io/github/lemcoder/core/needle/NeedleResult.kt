package io.github.lemcoder.core.needle

import io.github.lemcoder.core.model.needle.Needle

/**
 * Represents the result of executing a needle with type-safe value handling.
 * Each subclass contains the actual typed value returned from the script execution.
 */
sealed class NeedleResult {
    abstract val type: Needle.Arg.Type

    data class StringResult(val value: String) : NeedleResult() {
        override val type = Needle.Arg.Type.String
    }

    data class IntResult(val value: Int) : NeedleResult() {
        override val type = Needle.Arg.Type.Int
    }

    data class FloatResult(val value: Float) : NeedleResult() {
        override val type = Needle.Arg.Type.Float
    }

    data class BooleanResult(val value: Boolean) : NeedleResult() {
        override val type = Needle.Arg.Type.Boolean
    }
}

/**
 * Converts the needle result to a display string representation.
 */
fun NeedleResult.toDisplayString(): String = when (this) {
    is NeedleResult.StringResult -> value
    is NeedleResult.IntResult -> value.toString()
    is NeedleResult.FloatResult -> value.toString()
    is NeedleResult.BooleanResult -> value.toString()
}
