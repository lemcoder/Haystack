package io.github.lemcoder.core.service.agent

import io.github.lemcoder.core.model.needle.Needle
import kotlin.test.Test
import kotlin.test.assertNotNull

class ToolRegistryFactoryTest {

    private val factory = ToolRegistryFactory()

    @Test
    fun shouldCreateEmptyToolRegistry() {
        // Given
        val needles = emptyList<Needle>()

        // When
        val registry = factory.createToolRegistry(needles)

        // Then
        assertNotNull(registry)
    }

    // Note: Testing with actual needles requires full script engine setup
    // which is tested in integration tests. Here we just verify the factory
    // can be instantiated and called with empty needles.
}
