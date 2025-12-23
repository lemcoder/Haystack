package io.github.lemcoder.haystack.core.python

import io.github.lemcoder.core.model.needle.NeedleType

/**
 * Helper class for formatting Kotlin values to Python code strings.
 * Handles type conversion for needle argument injection.
 */
object PythonValueFormatter {

    /**
     * Formats a value as Python code based on the expected type.
     *
     * @param value The Kotlin value to format
     * @param type The expected NeedleType
     * @return Python code string representation of the value
     */
    fun format(value: Any, type: NeedleType?): String {
        return when (type) {
            NeedleType.String -> formatString(value.toString())
            NeedleType.Int -> value.toString()
            NeedleType.Float -> value.toString()
            NeedleType.Boolean -> value.toString()
            NeedleType.Image -> formatString(value.toString()) // Image path as string
            NeedleType.Any -> formatAny(value)
            null -> formatAny(value)
        }
    }

    /**
     * Formats a string value with proper Python escaping.
     * Uses triple quotes for multi-line support.
     */
    private fun formatString(value: String): String {
        val escaped = value.replace("\"\"\"", "\\\"\\\"\\\"")
        return "\"\"\"$escaped\"\"\""
    }

    /**
     * Formats a value of unknown/any type.
     * Best-effort conversion to Python.
     */
    private fun formatAny(value: Any): String {
        return when (value) {
            is String -> formatString(value)
            is Number, is Boolean -> value.toString()
            else -> formatString(value.toString())
        }
    }
}
