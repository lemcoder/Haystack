package io.github.lemcoder.haystack.core.service.needle

import ai.koog.prompt.message.Message
import android.util.Log
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parser for converting tool call arguments to needle parameters
 */
class NeedleArgumentParser {

    /**
     * Parses and validates arguments from a tool call for a given needle
     *
     * @param toolCall The tool call message from the LLM containing parameters
     * @param needle The needle definition with expected arguments
     * @return Map of argument names to their parsed values
     * @throws IllegalArgumentException if required arguments are missing or invalid
     */
    fun parseArguments(
        toolCall: Message.Tool.Call,
        needle: Needle
    ): Map<String, Any> {
        val params = extractParameters(toolCall, needle)
        validateArguments(needle, params)
        return params
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
                // Default values will be handled during Python code generation
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
            Log.w(TAG, "Failed to convert JSON value: $element", e)
            null
        }
    }

    /**
     * Validates that all required arguments are present
     */
    private fun validateArguments(needle: Needle, args: Map<String, Any>) {
        // Check required arguments
        needle.args.filter { it.required }.forEach { arg ->
            if (!args.containsKey(arg.name)) {
                throw IllegalArgumentException("Missing required argument: ${arg.name}")
            }
        }

        // Warn about unknown arguments
        args.forEach { (name, _) ->
            val argDef = needle.args.find { it.name == name }
            if (argDef == null) {
                Log.w(TAG, "Unknown argument provided: $name")
            }
        }
    }

    companion object {
        private const val TAG = "NeedleArgumentParser"
    }
}
