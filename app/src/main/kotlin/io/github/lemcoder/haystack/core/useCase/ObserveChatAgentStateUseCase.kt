package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.service.agent.ChatAgentService

class ObserveChatAgentStateUseCase(
  private val chatAgentService: ChatAgentService = ChatAgentService.Instance
) {
  operator fun invoke() = chatAgentService.agentState
}
