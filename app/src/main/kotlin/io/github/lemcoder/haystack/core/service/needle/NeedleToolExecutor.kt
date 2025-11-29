package io.github.lemcoder.haystack.core.service.needle

import ai.koog.prompt.message.Message
import android.util.Log
import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import io.github.lemcoder.haystack.core.python.PythonExecutor
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * Executor for needle tools that bypasses Koog's executeTool and handles
 * typed results (String, Int, Float, Boolean, Image, etc.)
 */
class NeedleToolExecutor {

    /**
     * Represents the result of executing a needle with its typed value
     */
    sealed class NeedleResult {
        abstract val needle: Needle
        abstract val rawOutput: String

        data class StringResult(
            override val needle: Needle,
            override val rawOutput: String,
            val value: String
        ) : NeedleResult()

        data class IntResult(
            override val needle: Needle,
            override val rawOutput: String,
            val value: Int
        ) : NeedleResult()

        data class FloatResult(
            override val needle: Needle,
            override val rawOutput: String,
            val value: Float
        ) : NeedleResult()

        data class BooleanResult(
            override val needle: Needle,
            override val rawOutput: String,
            val value: Boolean
        ) : NeedleResult()

        data class ImageResult(
            override val needle: Needle,
            override val rawOutput: String,
            val imagePath: String
        ) : NeedleResult()

        data class AnyResult(
            override val needle: Needle,
            override val rawOutput: String,
            val value: Any
        ) : NeedleResult()

        data class ErrorResult(
            override val needle: Needle,
            override val rawOutput: String,
            val error: String
        ) : NeedleResult()
    }

    /**
     * Executes a needle based on a tool call from the LLM
     *
     * @param toolCall The tool call message from the LLM containing tool name and parameters
     * @param needle The needle to execute
     * @return NeedleResult containing the typed result or error
     */
    suspend fun executeNeedle(
        toolCall: Message.Tool.Call,
        needle: Needle
    ): NeedleResult {
        return try {
            Log.d(TAG, "Executing needle: ${needle.name}")
            Log.d(TAG, "Tool call params: ${toolCall.content}")

            // Extract parameters from tool call
            val params = extractParameters(toolCall, needle)

            // Build Python code with parameters
            val pythonCode = buildPythonCode(needle, params)
            Log.d(TAG, "Generated Python code:\n$pythonCode")

            // Execute Python code
            val executionResult = PythonExecutor.executeSafe(pythonCode)

            executionResult.fold(
                onSuccess = { output ->
                    Log.d(TAG, "Needle execution successful: $output")
                    parseResult(needle, output)
                },
                onFailure = { error ->
                    Log.e(TAG, "Needle execution failed", error)
                    NeedleResult.ErrorResult(
                        needle = needle,
                        rawOutput = "",
                        error = error.message ?: "Unknown error"
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing needle", e)
            NeedleResult.ErrorResult(
                needle = needle,
                rawOutput = "",
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Extracts parameters from the tool call and maps them to needle arguments
     */
    private fun extractParameters(
        toolCall: Message.Tool.Call,
        needle: Needle
    ): Map<String, Any> {
        val params = mutableMapOf<String, Any>()
        val contentJson = toolCall.contentJson

        needle.args.forEach { arg ->
            val jsonValue = contentJson[arg.name]
            if (jsonValue != null) {
                val value = convertJsonToValue(jsonValue, arg.type)
                if (value != null) {
                    params[arg.name] = value
                }
            } else if (!arg.required && arg.defaultValue != null) {
                // Will be handled in buildPythonCode
            }
        }

        return params
    }

    /**
     * Converts a JSON element to the appropriate Kotlin type based on NeedleType
     */
    private fun convertJsonToValue(element: JsonElement, type: NeedleType): Any? {
        return try {
            when (type) {
                is NeedleType.Int -> element.jsonPrimitive.content.toIntOrNull()
                is NeedleType.Float -> element.jsonPrimitive.content.toFloatOrNull()
                is NeedleType.Boolean -> element.jsonPrimitive.content.toBooleanStrictOrNull()
                is NeedleType.String -> element.jsonPrimitive.content
                is NeedleType.Image -> element.jsonPrimitive.content
                is NeedleType.Any -> element.jsonPrimitive.content
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to convert JSON value: ${element}", e)
            null
        }
    }

    /**
     * Builds Python code with parameter assignments
     */
    private fun buildPythonCode(needle: Needle, params: Map<String, Any>): String {
        // Build variable assignments for each argument
        val argsCode = params.entries.joinToString("\n") { (name, value) ->
            val formattedValue = when (value) {
                is String -> "\"${value.replace("\"", "\\\"")}\""
                is Number -> value.toString()
                is Boolean -> if (value) "True" else "False"
                else -> "\"$value\""
            }
            "$name = $formattedValue"
        }

        // Add default values for optional arguments not provided
        val defaultsCode = needle.args
            .filter { !it.required && it.defaultValue != null && !params.containsKey(it.name) }
            .joinToString("\n") { arg ->
                "${arg.name} = ${arg.defaultValue}"
            }

        val parts = mutableListOf<String>()

        if (argsCode.isNotBlank()) {
            parts.add("# Arguments\n$argsCode")
        }

        if (defaultsCode.isNotBlank()) {
            parts.add("# Defaults\n$defaultsCode")
        }

        parts.add("# Needle code\n${needle.pythonCode}")

        return parts.joinToString("\n\n")
    }

    /**
     * Parses the raw Python output into a typed NeedleResult based on the needle's return type
     */
    private fun parseResult(needle: Needle, rawOutput: String): NeedleResult {
        return try {
            when (needle.returnType) {
                is NeedleType.String -> NeedleResult.StringResult(
                    needle = needle,
                    rawOutput = rawOutput,
                    value = rawOutput
                )

                is NeedleType.Int -> NeedleResult.IntResult(
                    needle = needle,
                    rawOutput = rawOutput,
                    value = rawOutput.trim().toInt()
                )

                is NeedleType.Float -> NeedleResult.FloatResult(
                    needle = needle,
                    rawOutput = rawOutput,
                    value = rawOutput.trim().toFloat()
                )

                is NeedleType.Boolean -> NeedleResult.BooleanResult(
                    needle = needle,
                    rawOutput = rawOutput,
                    value = rawOutput.trim().lowercase() in listOf("true", "1", "yes")
                )

                is NeedleType.Image -> NeedleResult.ImageResult(
                    needle = needle,
                    rawOutput = rawOutput,
                    imagePath = rawOutput.trim()
                )

                is NeedleType.Any -> NeedleResult.AnyResult(
                    needle = needle,
                    rawOutput = rawOutput,
                    value = rawOutput
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse result as ${needle.returnType}", e)
            NeedleResult.ErrorResult(
                needle = needle,
                rawOutput = rawOutput,
                error = "Failed to parse result: ${e.message}"
            )
        }
    }

    companion object {
        private const val TAG = "NeedleToolExecutor"
    }
}