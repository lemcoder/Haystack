package io.github.lemcoder.haystack.core.koog

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import io.github.lemcoder.haystack.core.python.PythonExecutor
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Adapter that converts a Needle into a Koog Tool.
 * Uses dynamic JSON-based arguments since we can't generate classes at runtime.
 */
class NeedleToolAdapter(
    private val needle: Needle
) : SimpleTool<NeedleToolAdapter.Args>() {

    @Serializable
    data class Args(
        val arguments: JsonObject
    ) : ToolArgs

    override val name: String = needle.name.replace(" ", "_").lowercase()

    override val argsSerializer: KSerializer<Args> = Args.serializer()

    override val description: String = buildDescription()

    override val descriptor: ToolDescriptor = ToolDescriptor(
        name = needle.name.replace(" ", "_").lowercase(),
        description = buildDescription(),
        requiredParameters = needle.args.filter { it.required }.map { arg ->
            ToolParameterDescriptor(
                name = arg.name,
                description = arg.description,
                type = arg.type.toKoogParameterType()
            )
        },
        optionalParameters = needle.args.filter { !it.required }.map { arg ->
            ToolParameterDescriptor(
                name = arg.name,
                description = arg.description,
                type = arg.type.toKoogParameterType()
            )
        }
    )

    private fun buildDescription(): String {
        val baseDescription = needle.description
        val argsDescription = needle.args.joinToString("\n") { arg ->
            "- ${arg.name} (${arg.type.toKoogTypeString()}): ${arg.description}" +
                    if (!arg.required) " [optional]" else ""
        }
        return "$baseDescription\n\nArguments:\n$argsDescription"
    }

    override suspend fun doExecute(args: Args): String {
        // Convert JSON arguments to Map<String, Any>
        val argsMap = mutableMapOf<String, Any>()

        args.arguments.entries.forEach { (key, jsonElement) ->
            val argDef = needle.args.find { it.name == key }
            val value = convertJsonToValue(jsonElement, argDef?.type)
            if (value != null) {
                argsMap[key] = value
            }
        }

        // Build Python code with arguments
        val pythonCode = buildPythonCode(argsMap)

        // Execute Python code
        val result = PythonExecutor.executeSafe(pythonCode)

        return result.getOrElse { error ->
            "Error executing ${needle.name}: ${error.message}"
        }
    }

    private fun convertJsonToValue(element: JsonElement, type: NeedleType?): Any? {
        return when (type) {
            is NeedleType.Int -> element.jsonPrimitive.content.toIntOrNull()
            is NeedleType.Float -> element.jsonPrimitive.content.toFloatOrNull()
            is NeedleType.Boolean -> element.jsonPrimitive.content.toBooleanStrictOrNull()
            is NeedleType.String -> element.jsonPrimitive.content
            else -> element.jsonPrimitive.content
        }
    }

    private fun buildPythonCode(args: Map<String, Any>): String {
        // Build variable assignments for each argument
        val argsCode = args.entries.joinToString("\n") { (name, value) ->
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
            .filter { !it.required && it.defaultValue != null && !args.containsKey(it.name) }
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

    private fun NeedleType.toKoogTypeString(): String {
        return when (this) {
            is NeedleType.String -> "string"
            is NeedleType.Int -> "integer"
            is NeedleType.Float -> "float"
            is NeedleType.Boolean -> "boolean"
            is NeedleType.Image -> "image"
            is NeedleType.Any -> "any"
        }
    }

    private fun NeedleType.toKoogParameterType(): ToolParameterType {
        return when (this) {
            is NeedleType.String -> ToolParameterType.String
            is NeedleType.Int -> ToolParameterType.Float // Koog doesn't have Int, using Float
            is NeedleType.Float -> ToolParameterType.Float
            is NeedleType.Boolean -> ToolParameterType.Boolean
            is NeedleType.Image -> ToolParameterType.String // Images as string paths
            is NeedleType.Any -> ToolParameterType.String
        }
    }
}
