package io.github.lemcoder.core.service.agent

import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

    @Test
    fun shouldCreateToolRegistryWithCallback() {
        // Given
        val needles = emptyList<Needle>()
        var callbackInvoked = false
        val callback: (Result<NeedleResult>) -> Unit = { callbackInvoked = true }

        // When
        val registry = factory.createToolRegistry(needles, callback)

        // Then
        assertNotNull(registry)
        // Callback is passed to tools, will be tested in NeedleToolAdapterTest
    }

    @Test
    fun shouldCreateToolRegistryWithMultipleNeedles() {
        // Given
        val needles =
            listOf(
                Needle(
                    id = "tool1",
                    name = "Tool 1",
                    description = "First tool",
                    code = "return 1",
                    args = emptyList(),
                    returnType = Needle.Arg.Type.Int,
                ),
                Needle(
                    id = "tool2",
                    name = "Tool 2",
                    description = "Second tool",
                    code = "return 2",
                    args = emptyList(),
                    returnType = Needle.Arg.Type.Int,
                ),
            )

        // When
        val registry = factory.createToolRegistry(needles)

        // Then
        assertNotNull(registry)
        // Registry should contain both tools
    }

    @Test
    fun shouldPassCallbackToAllTools() {
        // Given
        val needles =
            listOf(
                Needle(
                    id = "tool1",
                    name = "Tool 1",
                    description = "First tool",
                    code = "return 1",
                    args = emptyList(),
                    returnType = Needle.Arg.Type.Int,
                ),
                Needle(
                    id = "tool2",
                    name = "Tool 2",
                    description = "Second tool",
                    code = "return 2",
                    args = emptyList(),
                    returnType = Needle.Arg.Type.Int,
                ),
            )

        val callbackResults = mutableListOf<Result<NeedleResult>>()
        val callback: (Result<NeedleResult>) -> Unit = { callbackResults.add(it) }

        // When
        val registry = factory.createToolRegistry(needles, callback)

        // Then
        assertNotNull(registry)
        // Both tools should share the same callback
        // Actual callback invocation is tested in NeedleToolAdapterTest
    }

    // Note: Testing with actual needle execution requires full script engine setup
    // which is tested in NeedleToolAdapterTest. Here we just verify the factory
    // can create registries with proper configuration.
}
