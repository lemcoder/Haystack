package io.github.lemcoder.core.service.agent

import ai.koog.prompt.message.Message
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult
import io.github.lemcoder.core.needle.NeedleArgumentParser
import io.github.lemcoder.core.needle.NeedleToolExecutor
import io.github.lemcoder.core.utils.Log

/**
 * Coordinator responsible for needle execution workflow. Handles finding needles by tool name,
 * parsing arguments, and executing needles with proper error handling.
 *
 * @deprecated This class is no longer used in the main agent flow. Tool execution is now handled
 *   automatically by Koog through NeedleToolAdapter. Kept for potential testing or future use.
 */
@Deprecated(
    message =
        "Use NeedleToolAdapter directly. Koog handles tool execution automatically through the ToolRegistry.",
    level = DeprecationLevel.WARNING,
)
class NeedleExecutionCoordinator(
    private val argumentParser: NeedleArgumentParser = NeedleArgumentParser(),
    private val needleExecutor: NeedleToolExecutor = NeedleToolExecutor(),
) {

    /**
     * Executes a needle based on a tool call
     *
     * @param toolCall The tool call message from the LLM
     * @param needles Available needles to search from
     * @return Result containing the needle execution result or error
     */
    fun executeNeedle(toolCall: Message.Tool.Call, needles: List<Needle>): Result<NeedleResult> {
        Log.d(TAG, "Executing tool: ${toolCall.tool}")

        // Find the needle by tool name
        val needle = findNeedleByToolName(toolCall.tool, needles)
        if (needle == null) {
            Log.e(TAG, "Needle not found for tool: ${toolCall.tool}")
            return Result.failure(IllegalStateException("Tool not found: ${toolCall.tool}"))
        }

        return try {
            // Parse arguments
            val params = argumentParser.parseArguments(toolCall, needle)

            // Execute needle
            val result = needleExecutor.executeNeedle(needle, params)

            Log.d(TAG, "Needle execution completed: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error executing needle: ${needle.name}", e)
            Result.failure(e)
        }
    }

    /**
     * Finds a needle by its tool name (lowercase with underscores)
     *
     * @param toolName The tool name to search for
     * @param needles Available needles to search from
     * @return Matching needle or null if not found
     */
    private fun findNeedleByToolName(toolName: String, needles: List<Needle>): Needle? {
        return needles.find { needle -> needle.id == toolName }
    }

    companion object {
        private const val TAG = "NeedleExecutionCoordinator"
    }
}
