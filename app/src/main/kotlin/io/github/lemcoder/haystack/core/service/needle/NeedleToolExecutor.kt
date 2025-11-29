package io.github.lemcoder.haystack.core.service.needle

import ai.koog.prompt.message.Message
import android.util.Log
import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.python.PythonExecutor
import io.github.lemcoder.haystack.core.python.PythonValueFormatter

/**
 * Executor for needle tools that handles execution of needles with their arguments
 */
class NeedleToolExecutor(
    private val argumentParser: NeedleArgumentParser = NeedleArgumentParser()
) {

    /**
     * Executes a needle based on a tool call from the LLM
     *
     * @param toolCall The tool call message from the LLM containing tool name and parameters
     * @param needle The needle to execute
     * @return Result containing the output string or error
     */
    fun executeNeedle(
        toolCall: Message.Tool.Call,
        needle: Needle
    ): Result<String> {
        return try {
            Log.d(TAG, "Executing needle: ${needle.name}")
            Log.d(TAG, "Tool call params: ${toolCall.content}")

            // Parse and validate arguments
            val params = argumentParser.parseArguments(toolCall, needle)

            // Build Python code with parameters
            val pythonCode = buildPythonCode(needle, params)
            Log.d(TAG, "Generated Python code:\n$pythonCode")

            // Execute Python code
            val result = PythonExecutor.executeSafe(pythonCode)

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error executing needle", e)
            Result.failure(e)
        }
    }

    /**
     * Executes a needle with pre-parsed parameters
     *
     * @param needle The needle to execute
     * @param params The parsed and validated parameters
     * @return Result containing the output string or error
     */
    fun executeNeedle(
        needle: Needle,
        params: Map<String, Any>
    ): Result<String> {
        return try {
            Log.d(TAG, "Executing needle: ${needle.name}")

            // Build Python code with parameters
            val pythonCode = buildPythonCode(needle, params)
            Log.d(TAG, "Generated Python code:\n$pythonCode")

            // Execute Python code
            val result = PythonExecutor.executeSafe(pythonCode)

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error executing needle", e)
            Result.failure(e)
        }
    }

    /**
     * Builds Python code with parameter assignments
     */
    private fun buildPythonCode(needle: Needle, params: Map<String, Any>): String {
        // Build variable assignments for each argument
        val argsCode = params.entries.joinToString("\n") { (name, value) ->
            val argDef = needle.args.find { it.name == name }
            val formattedValue = PythonValueFormatter.format(value, argDef?.type)
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

    companion object {
        private const val TAG = "NeedleToolExecutor"
    }
}
