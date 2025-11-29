package io.github.lemcoder.haystack.core.service.agent

sealed class AgentState {
    data object Uninitialized : AgentState()
    data object Initializing : AgentState()
    data class Ready(val availableNeedles: List<String>) : AgentState()
    data class Processing(val toolCalls: List<String> = emptyList()) : AgentState()
    data class Completed(val response: String) : AgentState()
    data class Error(val message: String) : AgentState()
}