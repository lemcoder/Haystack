package io.github.lemcoder.core.koog

import ai.koog.agents.core.tools.SimpleTool
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult
import io.github.lemcoder.core.model.needle.toDisplayString
import io.github.lemcoder.core.needle.NeedleParameter
import io.github.lemcoder.core.needle.NeedleToolExecutor
import io.github.lemcoder.core.utils.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Adapter that converts a Needle into a Koog SimpleTool.
 *
 * This adapter enables seamless integration between Haystack's Needle system and Koog's AI agent
 * framework. It handles:
 * - Dynamic JSON-based argument parsing (since we can't generate classes at runtime)
 * - Type conversion from JSON to strongly-typed NeedleParameters
 * - Needle execution and result formatting
 * - Error handling and reporting
 *
 * When the LLM calls this tool, Koog automatically:
 * 1. Deserializes the arguments using the Args class
 * 2. Calls execute() with the parsed arguments
 * 3. Takes the returned String result
 * 4. Wraps it in a Message.Tool.Result
 * 5. Sends it back to the LLM for processing/explanation
 *
 * @param needle The Needle configuration defining the tool's behavior
 * @param onNeedleResult Optional callback invoked when needle execution completes
 */
class NeedleToolAdapter(
    private val needle: Needle,
    private val onNeedleResult: ((Result<NeedleResult>) -> Unit)? = null,
) :
    SimpleTool<NeedleToolAdapter.Args>(
        argsSerializer = Args.serializer(),
        name = needle.id,
        description = needle.description,
    ) {

    /**
     * Arguments wrapper for dynamic JSON-based tool arguments. Koog deserializes LLM tool call
     * arguments into this structure.
     */
    @Serializable data class Args(val arguments: JsonObject)

    private val executor = NeedleToolExecutor()

    /**
     * Converts a JSON element to a strongly-typed NeedleParameter based on the expected type.
     *
     * @param element The JSON element from the tool call arguments
     * @param name The parameter name
     * @param type The expected parameter type from the Needle definition
     * @return Converted NeedleParameter or null if conversion fails
     */
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

    /**
     * Executes the needle with the provided arguments.
     *
     * This method is called automatically by Koog when the LLM requests this tool. The workflow:
     * 1. Parse JSON arguments into strongly-typed NeedleParameters
     * 2. Execute the needle with the parsed parameters
     * 3. Invoke the callback (if provided) with the typed result
     * 4. Return a string representation for the LLM to process
     *
     * The returned string will be wrapped by Koog in a Message.Tool.Result and sent back to the
     * LLM, allowing it to generate a natural language explanation of the result.
     *
     * @param args The deserialized arguments from the LLM's tool call
     * @return String representation of the needle result, or error message
     */
    override suspend fun execute(args: Args): String {
        Log.d(TAG, "Executing needle tool: ${needle.name} (${needle.id})")

        // Parse JSON arguments into strongly-typed parameters
        val params = mutableListOf<NeedleParameter>()
        needle.args.forEach { arg ->
            val jsonValue = args.arguments[arg.name]
            if (jsonValue != null) {
                val param = convertJsonToParameter(jsonValue, arg.name, arg.type)
                if (param != null) {
                    params.add(param)
                } else {
                    Log.w(TAG, "Failed to convert parameter: ${arg.name}")
                }
            }
        }

        // Execute the needle
        val result = executor.executeNeedle(needle = needle, params = params)

        // Notify callback with the typed result
        onNeedleResult?.invoke(result)

        // Return string representation for LLM to process
        val resultString =
            result.fold(
                onSuccess = { it.toDisplayString() },
                onFailure = { "Error executing needle: ${it.message}" },
            )

        Log.d(TAG, "Needle execution completed: ${result.isSuccess}")
        return resultString
    }

    companion object {
        private const val TAG = "NeedleToolAdapter"
    }
}
