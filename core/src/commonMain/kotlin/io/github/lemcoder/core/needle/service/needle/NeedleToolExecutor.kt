package io.github.lemcoder.core.needle.service.needle

import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.utils.Log

/** Executor for needle tools that handles execution of needles with their arguments */
class NeedleToolExecutor() {

    /**
     * Executes a needle with pre-parsed parameters
     *
     * @param needle The needle to execute
     * @param params The parsed and validated parameters
     * @return Result containing a pair of (NeedleType, actual value as string) or error
     */
    fun executeNeedle(
        params: Map<String, Any>,
        needle: Needle,
    ): Result<Pair<Needle.Arg.Type, String>> {
        return try {
            Log.d(TAG, "Executing needle: ${needle.name}")

            // Build Python code with parameters
            val pythonCode = buildPythonCode(needle, params)
            Log.d(TAG, "Generated Python code:\n$pythonCode")

            // Execute Python code
            val result: Result<String> = TODO()

            // Parse the result based on the needle's return type
            result.mapCatching { output ->
                val needleType = parseNeedleType(output, needle.returnType)
                Pair(needleType, output.trim())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing needle", e)
            Result.failure(e)
        }
    }

    /**
     * Parses a string output from Python into the appropriate NeedleType
     *
     * @param output The string output from PythonExecutor
     * @param expectedType The expected return type of the needle
     * @return The parsed NeedleType value
     * @throws IllegalArgumentException if the output cannot be parsed to the expected type
     */
    private fun parseNeedleType(output: String, expectedType: Needle.Arg.Type): Needle.Arg.Type {
        val trimmedOutput = output.trim()

        return when (expectedType) {
            is Needle.Arg.Type.String -> {
                Needle.Arg.Type.String
            }

            is Needle.Arg.Type.Int -> {
                trimmedOutput.toIntOrNull()
                    ?: throw IllegalArgumentException("Cannot parse '$trimmedOutput' as Int")
                Needle.Arg.Type.Int
            }

            is Needle.Arg.Type.Float -> {
                trimmedOutput.toFloatOrNull()
                    ?: throw IllegalArgumentException("Cannot parse '$trimmedOutput' as Float")
                Needle.Arg.Type.Float
            }

            is Needle.Arg.Type.Boolean -> {
                when (trimmedOutput.lowercase()) {
                    "true",
                    "1" -> Needle.Arg.Type.Boolean
                    "false",
                    "0" -> Needle.Arg.Type.Boolean
                    else ->
                        throw IllegalArgumentException("Cannot parse '$trimmedOutput' as Boolean")
                }
            }

            is Needle.Arg.Type.Image -> {
                // Image type might need special handling - for now just return the type
                Needle.Arg.Type.Image
            }

            is Needle.Arg.Type.Any -> {
                // Any type accepts anything
                Needle.Arg.Type.Any
            }
        }
    }

    /** Builds Python code with parameter assignments */
    private fun buildPythonCode(needle: Needle, params: Map<String, Any>): String {
        // Build variable assignments for each argument
        val argsCode =
            params.entries.joinToString("\n") { (name, value) ->
                val argDef = needle.args.find { it.name == name }
                val formattedValue: String = TODO()
                "$name = $formattedValue"
            }

        // Add default values for optional arguments not provided
        val defaultsCode =
            needle.args
                .filter { !it.required && it.defaultValue != null && !params.containsKey(it.name) }
                .joinToString("\n") { arg -> "${arg.name} = ${arg.defaultValue}" }

        val parts = mutableListOf<String>()

        if (argsCode.isNotBlank()) {
            parts.add("# Arguments\n$argsCode")
        }

        if (defaultsCode.isNotBlank()) {
            parts.add("# Defaults\n$defaultsCode")
        }

        parts.add("# Needle code\n${needle.code}")

        return parts.joinToString("\n\n")
    }

    companion object {
        private const val TAG = "NeedleToolExecutor"
    }
}
