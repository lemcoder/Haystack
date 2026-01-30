package io.github.lemcoder.core.service.agent

import io.github.lemcoder.core.model.llm.ExecutorType
import io.github.lemcoder.core.model.llm.PromptExecutorConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AgentConfigFactoryTest {

    private val factory = AgentConfigFactory()

    @Test
    fun shouldCreateAgentConfigWithOllamaExecutor() {
        // Given
        val executorConfig =
            PromptExecutorConfig(
                executorType = ExecutorType.Ollama("http://localhost:11434"),
                selectedModelName = "llama2",
            )

        // When
        val config = factory.createAgentConfig(executorConfig)

        // Then
        assertNotNull(config)
        assertEquals("llama2", config.model.id)
        assertEquals(16_000, config.model.contextLength)
        assertEquals(16_000, config.model.maxOutputTokens)
        assertEquals(10, config.maxAgentIterations)
        assertTrue(config.model.capabilities.isNotEmpty())
    }

    @Test
    fun shouldCreateAgentConfigWithOpenAIExecutor() {
        // Given
        val executorConfig =
            PromptExecutorConfig(
                executorType = ExecutorType.OpenAI("fake-api-key"),
                selectedModelName = "gpt-4",
            )

        // When
        val config = factory.createAgentConfig(executorConfig)

        // Then
        assertNotNull(config)
        assertEquals("gpt-4", config.model.id)
        // OpenAI should have additional capabilities
        assertTrue(config.model.capabilities.isNotEmpty())
    }

    @Test
    fun shouldCreateAgentConfigWithOpenRouterExecutor() {
        // Given
        val executorConfig =
            PromptExecutorConfig(
                executorType = ExecutorType.OpenRouter("fake-api-key"),
                selectedModelName = "anthropic/claude-3",
            )

        // When
        val config = factory.createAgentConfig(executorConfig)

        // Then
        assertNotNull(config)
        assertEquals("anthropic/claude-3", config.model.id)
        assertTrue(config.model.capabilities.isNotEmpty())
    }

    @Test
    fun shouldIncludeSystemPromptInConfig() {
        // Given
        val executorConfig =
            PromptExecutorConfig(
                executorType = ExecutorType.Ollama("http://localhost:11434"),
                selectedModelName = "llama2",
            )

        // When
        val config = factory.createAgentConfig(executorConfig)

        // Then
        assertNotNull(config.prompt)
    }
}
