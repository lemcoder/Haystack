package io.github.lemcoder.core.needle

import ai.koog.prompt.message.Message
import io.github.lemcoder.core.model.needle.Needle
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

/** Parser for converting tool call arguments to needle parameters */
class NeedleArgumentParser {

    /**
     * Parses and validates arguments from a tool call for a given needle
     *
     * @param toolCall The tool call message from the LLM containing parameters
     * @param needle The needle definition with expected arguments
     * @return List of type-safe NeedleParameter objects
     * @throws IllegalArgumentException if required arguments are missing or invalid
     */
    fun parseArguments(toolCall: Message.Tool.Call, needle: Needle): List<NeedleParameter> {
        val params = extractParameters(toolCall, needle)
        validateArguments(needle, params)
        return params
    }

    /**
     * Parses and validates arguments from a tool call for a given needle (legacy support)
     *
     * @param toolCall The tool call message from the LLM containing parameters
     * @param needle The needle definition with expected arguments
     * @return Map of argument names to their parsed values
     * @throws IllegalArgumentException if required arguments are missing or invalid
     * @deprecated Use parseArguments which returns List<NeedleParameter> for type safety
     */
    @Deprecated(
        "Use parseArguments for type-safe parameters",
        ReplaceWith("parseArguments(toolCall, needle).toParamMap()"),
    )
    fun parseArgumentsLegacy(toolCall: Message.Tool.Call, needle: Needle): Map<String, Any> {
        return parseArguments(toolCall, needle).toParamMap()
    }

    /** Extracts parameters from the tool call and maps them to needle arguments */
    private fun extractParameters(
        toolCall: Message.Tool.Call,
        needle: Needle,
    ): List<NeedleParameter> {
        val params = mutableListOf<NeedleParameter>()
        val contentJson = toolCall.contentJson

        needle.args.forEach { arg ->
            val jsonValue = contentJson[arg.name]
            if (jsonValue != null) {
                val param = convertJsonToParameter(jsonValue, arg.name, arg.type)
                if (param != null) {
                    params.add(param)
                }
            }
        }

        return params
    }

    /**
     * Converts a JSON element to a type-safe NeedleParameter based on the expected type
     *
     * @return NeedleParameter with the properly typed value, or null if conversion fails
     */
    private fun convertJsonToParameter(
        element: JsonElement,
        name: String,
        type: Needle.Arg.Type,
    ): NeedleParameter? {
        return try {
            when (type) {
                is Needle.Arg.Type.Int -> {
                    element.jsonPrimitive.content.toIntOrNull()?.let {
                        NeedleParameter.IntParam(name, it)
                    }
                }
                is Needle.Arg.Type.Float -> {
                    element.jsonPrimitive.content.toFloatOrNull()?.let {
                        NeedleParameter.FloatParam(name, it)
                    }
                }
                is Needle.Arg.Type.Boolean -> {
                    element.jsonPrimitive.content.toBooleanStrictOrNull()?.let {
                        NeedleParameter.BooleanParam(name, it)
                    }
                }
                is Needle.Arg.Type.String -> {
                    NeedleParameter.StringParam(name, element.jsonPrimitive.content)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Parses and validates arguments provided as a Map<String, Any> for a given needle. This method
     * converts entries to type-safe NeedleParameter instances and validates required args.
     */
    fun parseFromMap(args: Map<String, Any>, needle: Needle): List<NeedleParameter> {
        val params = mutableListOf<NeedleParameter>()

        needle.args.forEach { arg ->
            val raw = args[arg.name]
            if (raw != null) {
                val param = convertAnyToParameter(raw, arg.name, arg.type)
                if (param != null) {
                    params.add(param)
                } else {
                    throw IllegalArgumentException("Invalid value for argument: ${arg.name}")
                }
            }
        }

        validateArguments(needle, params)
        return params
    }

    /**
     * Converts a raw Any value to a NeedleParameter according to expected type. Supports Number,
     * Boolean and String inputs (strings will be parsed when possible).
     */
    private fun convertAnyToParameter(
        value: Any,
        name: String,
        type: Needle.Arg.Type,
    ): NeedleParameter? {
        return try {
            when (type) {
                is Needle.Arg.Type.Int -> {
                    when (value) {
                        is Number -> NeedleParameter.IntParam(name, value.toInt())
                        is String -> value.toIntOrNull()?.let { NeedleParameter.IntParam(name, it) }
                        else -> null
                    }
                }
                is Needle.Arg.Type.Float -> {
                    when (value) {
                        is Number -> NeedleParameter.FloatParam(name, value.toFloat())
                        is String ->
                            value.toFloatOrNull()?.let { NeedleParameter.FloatParam(name, it) }
                        else -> null
                    }
                }
                is Needle.Arg.Type.Boolean -> {
                    when (value) {
                        is Boolean -> NeedleParameter.BooleanParam(name, value)
                        is String -> {
                            when (value.trim().lowercase()) {
                                "true" -> NeedleParameter.BooleanParam(name, true)
                                "false" -> NeedleParameter.BooleanParam(name, false)
                                else -> null
                            }
                        }
                        else -> null
                    }
                }
                is Needle.Arg.Type.String -> {
                    NeedleParameter.StringParam(name, value.toString())
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    /** Validates that all required arguments are present */
    private fun validateArguments(needle: Needle, params: List<NeedleParameter>) {
        val paramNames = params.map { it.name }.toSet()

        // Check required arguments
        needle.args
            .filter { it.required }
            .forEach { arg ->
                if (!paramNames.contains(arg.name)) {
                    throw IllegalArgumentException("Missing required argument: ${arg.name}")
                }
            }
    }
}
