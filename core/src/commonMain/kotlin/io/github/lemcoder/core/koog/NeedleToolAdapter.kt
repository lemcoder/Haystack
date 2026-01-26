package io.github.lemcoder.core.koog

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolParameterType
import io.github.lemcoder.core.model.needle.Needle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Adapter that converts a Needle into a Koog Tool. Uses dynamic JSON-based arguments since we can't
 * generate classes at runtime.
 */
class NeedleToolAdapter(private val needle: Needle) : SimpleTool<NeedleToolAdapter.Args>(
    argsSerializer = Args.serializer(),
    name = needle.name,
    description = needle.description
) {
    @Serializable
    data class Args(val arguments: JsonObject)

    private fun convertJsonToValue(element: JsonElement, type: Needle.Arg.Type?): Any? {
        return when (type) {
            is Needle.Arg.Type.Int -> element.jsonPrimitive.content.toIntOrNull()
            is Needle.Arg.Type.Float -> element.jsonPrimitive.content.toFloatOrNull()
            is Needle.Arg.Type.Boolean -> element.jsonPrimitive.content.toBooleanStrictOrNull()
            is Needle.Arg.Type.String -> element.jsonPrimitive.content
            else -> element.jsonPrimitive.content
        }
    }

    private fun Needle.Arg.Type.toKoogTypeString(): String {
        return when (this) {
            is Needle.Arg.Type.String -> "string"
            is Needle.Arg.Type.Int -> "integer"
            is Needle.Arg.Type.Float -> "float"
            is Needle.Arg.Type.Boolean -> "boolean"
            is Needle.Arg.Type.Image -> "image"
            is Needle.Arg.Type.Any -> "any"
        }
    }

    private fun Needle.Arg.Type.toKoogParameterType(): ToolParameterType {
        return when (this) {
            is Needle.Arg.Type.String -> ToolParameterType.String
            is Needle.Arg.Type.Int -> ToolParameterType.Float // Koog doesn't have Int, using Float
            is Needle.Arg.Type.Float -> ToolParameterType.Float
            is Needle.Arg.Type.Boolean -> ToolParameterType.Boolean
            is Needle.Arg.Type.Image -> ToolParameterType.String // Images as string paths
            is Needle.Arg.Type.Any -> ToolParameterType.String
        }
    }

    override suspend fun execute(args: Args): String {
        TODO("Not yet implemented")
    }
}
