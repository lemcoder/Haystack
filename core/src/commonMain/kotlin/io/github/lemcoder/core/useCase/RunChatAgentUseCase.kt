package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.model.needle.NeedleType
import io.github.lemcoder.core.service.agent.ChatAgentService

/**
 * Use case for running the chat agent with a user message. Handles initialization if needed and
 * delegates to the service.
 */
class RunChatAgentUseCase(
    private val chatAgentService: ChatAgentService = ChatAgentService.Instance
) {
    suspend operator fun invoke(
        userMessage: String,
        onToolResult: ((Result<Pair<NeedleType, String>>) -> Unit)? = null,
    ): String {
        // Set needle result callback if provided
        if (onToolResult != null) {
            chatAgentService.setNeedleResultCallback(onToolResult)
        }

        // Initialize agent if not ready
        if (!chatAgentService.isReady()) {
            chatAgentService.initializeAgent()
        }

        // Run the agent with the user message
        return chatAgentService.runAgent(userMessage)
    }
}
