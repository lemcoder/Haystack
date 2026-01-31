package io.github.lemcoder.core.service.agent

import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.asAssistantMessage
import ai.koog.agents.core.dsl.extension.requestLLM
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Factory responsible for creating chat strategies for the Haystack agent.
 *
 * This factory creates a functional strategy that properly integrates with Koog's tool execution
 * system. Instead of manually intercepting tool calls, it lets Koog handle the complete tool
 * execution flow:
 * 1. Agent receives user input
 * 2. LLM decides whether to call a tool or respond directly
 * 3. If tool call is needed:
 *     - Koog automatically executes the tool via ToolRegistry
 *     - Tool result is sent back to LLM wrapped in Message.Tool.Result
 *     - LLM processes the result and generates an explanation/response
 * 4. Final assistant message is returned to user
 *
 * This ensures the LLM can properly explain and contextualize tool results instead of just
 * returning raw data.
 */
class ChatStrategyFactory {

    /**
     * Creates a functional strategy for handling chat interactions with proper tool integration
     *
     * @param agentState Mutable state flow to update agent state during execution
     * @return Configured functional strategy that lets Koog handle tool execution
     */
    fun createChatStrategy(agentState: MutableStateFlow<AgentState>) =
        functionalStrategy<String, String>("haystack-chat") { input ->
            // Update state to indicate processing
            agentState.value = AgentState.Processing()

            // Request LLM response - Koog automatically handles tool calls:
            // - If LLM requests a tool, Koog executes it from ToolRegistry
            // - Tool result is sent back to LLM
            // - LLM generates explanation based on tool result
            // - Returns final assistant message
            val response = requestLLM(input)

            // Extract and return the final assistant message content
            response.asAssistantMessage().content
        }
}
