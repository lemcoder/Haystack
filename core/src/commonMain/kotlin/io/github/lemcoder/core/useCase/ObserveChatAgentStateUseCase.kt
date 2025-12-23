package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.service.agent.ChatAgentService

class ObserveChatAgentStateUseCase(
    private val chatAgentService: ChatAgentService = ChatAgentService.Instance
) {
    operator fun invoke() = chatAgentService.agentState
}
