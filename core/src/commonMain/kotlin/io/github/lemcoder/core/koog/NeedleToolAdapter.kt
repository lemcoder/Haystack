package io.github.lemcoder.core.koog

import ai.koog.agents.core.tools.SimpleTool
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.toDisplayString
import io.github.lemcoder.core.needle.NeedleToolExecutor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Adapter that converts a Needle into a Koog Tool. Uses dynamic JSON-based arguments since we can't
 * generate classes at runtime.
 */
class NeedleToolAdapter(private val needle: Needle) :
    SimpleTool<NeedleToolAdapter.Args>(
        argsSerializer = Args.serializer(),
        name = needle.name,
        description = needle.description,
    ) {
    @Serializable data class Args(val arguments: JsonObject)

    private val executor = NeedleToolExecutor()

    private fun convertJsonToValue(element: JsonElement, type: Needle.Arg.Type?): Any? {
        return when (type) {
            is Needle.Arg.Type.Int -> element.jsonPrimitive.content.toIntOrNull()
            is Needle.Arg.Type.Float -> element.jsonPrimitive.content.toFloatOrNull()
            is Needle.Arg.Type.Boolean -> element.jsonPrimitive.content.toBooleanStrictOrNull()
            is Needle.Arg.Type.String -> element.jsonPrimitive.content
            else -> element.jsonPrimitive.content
        }
    }

    override suspend fun execute(args: Args): String {
        val argsAsMap = mutableMapOf<String, Any>()
        needle.args.forEach { arg ->
            val jsonValue = args.arguments[arg.name]
            if (jsonValue != null) {
                val value = convertJsonToValue(jsonValue, arg.type)
                if (value != null) {
                    argsAsMap[arg.name] = value
                }
            }
        }

        val result = executor.executeNeedle(needle = needle, params = argsAsMap)

        return result.fold(
            onSuccess = { it.toDisplayString() },
            onFailure = { "Error executing needle: ${it.message}" },
        )
    }
}
