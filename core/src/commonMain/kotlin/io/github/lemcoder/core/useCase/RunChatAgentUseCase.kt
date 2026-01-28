package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.model.needle.NeedleResult
import io.github.lemcoder.core.service.agent.ChatAgentService

/**
 * Use case for running the chat agent with a user message. Handles initialization if needed and
 * delegates to the service.
 */
interface RunChatAgentUseCase {
    suspend operator fun invoke(
        userMessage: String,
        onToolResult: ((Result<NeedleResult>) -> Unit)? = null,
    ): String

    companion object {
        fun create(): RunChatAgentUseCase {
            return RunChatAgentUseCaseImpl()
        }
    }
}

private class RunChatAgentUseCaseImpl(
    private val chatAgentService: ChatAgentService = ChatAgentService.Instance
) : RunChatAgentUseCase {
    override suspend fun invoke(
        userMessage: String,
        onToolResult: ((Result<NeedleResult>) -> Unit)?,
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
