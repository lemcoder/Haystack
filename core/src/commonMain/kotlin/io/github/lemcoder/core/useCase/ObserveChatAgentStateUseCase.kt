package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.service.agent.AgentState
import io.github.lemcoder.core.service.agent.ChatAgentService
import kotlinx.coroutines.flow.StateFlow

interface ObserveChatAgentStateUseCase {
    operator fun invoke(): StateFlow<AgentState>

    companion object {
        fun create(): ObserveChatAgentStateUseCase {
            return ObserveChatAgentStateUseCaseImpl()
        }
    }
}

private class ObserveChatAgentStateUseCaseImpl(
    private val chatAgentService: ChatAgentService = ChatAgentService.Instance
) : ObserveChatAgentStateUseCase {
    override fun invoke(): StateFlow<AgentState> = chatAgentService.agentState
}
