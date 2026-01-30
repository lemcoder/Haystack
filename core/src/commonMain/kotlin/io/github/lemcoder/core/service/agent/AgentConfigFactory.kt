package io.github.lemcoder.core.service.agent

import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import io.github.lemcoder.core.model.llm.ExecutorType
import io.github.lemcoder.core.model.llm.PromptExecutorConfig

/**
 * Factory responsible for creating AIAgentConfig instances based on executor configuration. Handles
 * model configuration, capabilities, and system prompt setup.
 */
class AgentConfigFactory {

    /**
     * Creates an AIAgentConfig from the provided executor configuration
     *
     * @param executorConfig The executor configuration containing model and provider details
     * @return Configured AIAgentConfig instance
     */
    fun createAgentConfig(executorConfig: PromptExecutorConfig): AIAgentConfig {
        val model = executorConfig.selectedModelName
        val llmModel =
            LLModel(
                provider = toLLMProvider(executorConfig),
                id = model,
                capabilities = buildCapabilities(executorConfig.executorType),
                contextLength = 16_000,
                maxOutputTokens = 16_000,
            )

        return AIAgentConfig(
            prompt =
                prompt("haystack-chat") {
                    system("You are an AI assistant that helps users by calling tools as needed.")
                },
            model = llmModel,
            maxAgentIterations = 10,
        )
    }

    /**
     * Builds the list of LLM capabilities based on executor type
     *
     * @param executorType The type of executor (Local, Ollama, OpenAI, OpenRouter)
     * @return List of applicable LLM capabilities
     */
    private fun buildCapabilities(executorType: ExecutorType): List<LLMCapability> {
        val baseCapabilities =
            mutableListOf(LLMCapability.Tools, LLMCapability.ToolChoice, LLMCapability.Completion)

        // Add executor-specific capabilities
        when (executorType) {
            is ExecutorType.Local -> {
                // Add local-specific capabilities if any
            }
            is ExecutorType.Ollama -> {
                // Add Ollama-specific capabilities if any
            }
            is ExecutorType.OpenAI -> {
                baseCapabilities.add(LLMCapability.OpenAIEndpoint.Completions)
                baseCapabilities.add(LLMCapability.OpenAIEndpoint.Responses)
            }
            is ExecutorType.OpenRouter -> {
                // Add OpenRouter-specific capabilities if any
            }
        }

        return baseCapabilities
    }

    /**
     * Converts executor configuration to LLM provider
     *
     * @param executorConfig The executor configuration
     * @return Corresponding LLMProvider
     */
    private fun toLLMProvider(executorConfig: PromptExecutorConfig): LLMProvider {
        return when (executorConfig.executorType) {
            is ExecutorType.Local -> TODO()
            is ExecutorType.Ollama -> LLMProvider.Ollama
            is ExecutorType.OpenAI -> LLMProvider.OpenAI
            is ExecutorType.OpenRouter -> LLMProvider.OpenRouter
        }
    }
}
