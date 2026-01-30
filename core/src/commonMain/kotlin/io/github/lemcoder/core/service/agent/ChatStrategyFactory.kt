package io.github.lemcoder.core.service.agent

import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.asAssistantMessage
import ai.koog.agents.core.dsl.extension.requestLLM
import ai.koog.prompt.message.Message
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult
import io.github.lemcoder.core.model.needle.toDisplayString
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Factory responsible for creating chat strategies. Handles the interaction flow between the LLM
 * and needle execution, including tool call interception and state management.
 */
class ChatStrategyFactory(
    private val needleExecutionCoordinator: NeedleExecutionCoordinator =
        NeedleExecutionCoordinator()
) {

    /**
     * Creates a functional strategy for handling chat interactions with tool calls
     *
     * @param needles Available needles for tool execution
     * @param agentState Mutable state flow to update agent state during execution
     * @param onNeedleResult Callback to receive needle execution results
     * @return Configured functional strategy
     */
    fun createChatStrategy(
        needles: List<Needle>,
        agentState: MutableStateFlow<AgentState>,
        onNeedleResult: ((Result<NeedleResult>) -> Unit)?,
    ) =
        functionalStrategy<String, String>("haystack-chat") { input ->
            val toolCalls = mutableListOf<String>()
            val response = requestLLM(input)

            // Handle tool calls with our custom executor
            if (response is Message.Tool.Call) {
                val toolName = response.tool
                toolCalls.add(toolName)

                // Update state with current tool call
                agentState.value = AgentState.Processing(toolCalls)

                // Execute the needle
                val needleResult = needleExecutionCoordinator.executeNeedle(response, needles)

                // Emit needle result via callback
                onNeedleResult?.invoke(needleResult)

                // Return the result as string (for MVP, end workflow here)
                return@functionalStrategy needleResult.fold(
                    onSuccess = { result ->
                        result.toDisplayString() // Convert the typed result to string
                    },
                    onFailure = { error -> "Error: ${error.message}" },
                )
            }

            // Get final assistant message if no tool was called
            response.asAssistantMessage().content
        }
}
