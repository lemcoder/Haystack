package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.service.agent.ChatAgentService

/**
 * Use case for running the chat agent with a user message.
 * Handles initialization if needed and delegates to the service.
 */
class RunChatAgentUseCase(
    private val chatAgentService: ChatAgentService = ChatAgentService.Instance
) {
    suspend operator fun invoke(userMessage: String): String {
        // Initialize agent if not ready
        if (!chatAgentService.isReady()) {
            chatAgentService.initializeAgent()
        }

        // Run the agent with the user message
        return chatAgentService.runAgent(userMessage)
    }
}
