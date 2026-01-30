package io.github.lemcoder.core.koog

import ai.koog.agents.core.tools.SimpleTool
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.toDisplayString
import io.github.lemcoder.core.needle.NeedleParameter
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
        name = needle.id,
        description = needle.description,
    ) {
    @Serializable data class Args(val arguments: JsonObject)

    private val executor = NeedleToolExecutor()

    private fun convertJsonToParameter(
        element: JsonElement,
        name: String,
        type: Needle.Arg.Type?,
    ): NeedleParameter? {
        return try {
            when (type) {
                is Needle.Arg.Type.Int ->
                    element.jsonPrimitive.content.toIntOrNull()?.let {
                        NeedleParameter.IntParam(name, it)
                    }
                is Needle.Arg.Type.Float ->
                    element.jsonPrimitive.content.toFloatOrNull()?.let {
                        NeedleParameter.FloatParam(name, it)
                    }
                is Needle.Arg.Type.Boolean ->
                    element.jsonPrimitive.content.toBooleanStrictOrNull()?.let {
                        NeedleParameter.BooleanParam(name, it)
                    }
                is Needle.Arg.Type.String ->
                    NeedleParameter.StringParam(name, element.jsonPrimitive.content)
                else -> NeedleParameter.StringParam(name, element.jsonPrimitive.content)
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun execute(args: Args): String {
        val params = mutableListOf<NeedleParameter>()
        needle.args.forEach { arg ->
            val jsonValue = args.arguments[arg.name]
            if (jsonValue != null) {
                val param = convertJsonToParameter(jsonValue, arg.name, arg.type)
                if (param != null) {
                    params.add(param)
                }
            }
        }

        val result = executor.executeNeedle(needle = needle, params = params)

        return result.fold(
            onSuccess = { it.toDisplayString() },
            onFailure = { "Error executing needle: ${it.message}" },
        )
    }
}
