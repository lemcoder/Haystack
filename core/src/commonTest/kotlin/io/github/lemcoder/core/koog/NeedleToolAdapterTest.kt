package io.github.lemcoder.core.koog

import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class NeedleToolAdapterTest {

    @Test
    fun shouldCreateToolWithCorrectNameAndDescription() {
        // Given
        val needle =
            Needle(
                id = "test-tool",
                name = "Test Tool",
                description = "A test tool for testing",
                code = "return 'test'",
                args = emptyList(),
                returnType = Needle.Arg.Type.String,
            )

        // When
        val adapter = NeedleToolAdapter(needle)

        // Then
        assertEquals("test-tool", adapter.name)
        assertEquals("A test tool for testing", adapter.description)
    }

    @Test
    fun shouldExecuteNeedleWithNoArguments() = runTest {
        // Given
        val needle =
            Needle(
                id = "simple-tool",
                name = "Simple Tool",
                description = "Returns a simple string",
                code = "return 'Hello World'",
                args = emptyList(),
                returnType = Needle.Arg.Type.String,
            )

        val adapter = NeedleToolAdapter(needle)
        val args = NeedleToolAdapter.Args(JsonObject(emptyMap()))

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("Hello World", result)
    }

    @Test
    fun shouldExecuteNeedleWithStringArgument() = runTest {
        // Given
        val needle =
            Needle(
                id = "echo-tool",
                name = "Echo Tool",
                description = "Echoes the input",
                code = "return message",
                args =
                    listOf(
                        Needle.Arg(
                            name = "message",
                            type = Needle.Arg.Type.String,
                            description = "Message to echo",
                        )
                    ),
                returnType = Needle.Arg.Type.String,
            )

        val adapter = NeedleToolAdapter(needle)
        val args =
            NeedleToolAdapter.Args(
                JsonObject(mapOf("message" to JsonPrimitive("Hello from test")))
            )

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("Hello from test", result)
    }

    @Test
    fun shouldExecuteNeedleWithIntArgument() = runTest {
        // Given
        val needle =
            Needle(
                id = "double-tool",
                name = "Double Tool",
                description = "Doubles a number",
                code = "return value * 2",
                args =
                    listOf(
                        Needle.Arg(
                            name = "value",
                            type = Needle.Arg.Type.Int,
                            description = "Number to double",
                        )
                    ),
                returnType = Needle.Arg.Type.Int,
            )

        val adapter = NeedleToolAdapter(needle)
        val args = NeedleToolAdapter.Args(JsonObject(mapOf("value" to JsonPrimitive("21"))))

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("42", result)
    }

    @Test
    fun shouldExecuteNeedleWithMultipleArguments() = runTest {
        // Given
        val needle =
            Needle(
                id = "add-tool",
                name = "Add Tool",
                description = "Adds two numbers",
                code = "return a + b",
                args =
                    listOf(
                        Needle.Arg(name = "a", type = Needle.Arg.Type.Int, description = "First number"),
                        Needle.Arg(
                            name = "b",
                            type = Needle.Arg.Type.Int,
                            description = "Second number",
                        ),
                    ),
                returnType = Needle.Arg.Type.Int,
            )

        val adapter = NeedleToolAdapter(needle)
        val args =
            NeedleToolAdapter.Args(
                JsonObject(mapOf("a" to JsonPrimitive("10"), "b" to JsonPrimitive("32")))
            )

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("42", result)
    }

    @Test
    fun shouldInvokeCallbackOnSuccess() = runTest {
        // Given
        val needle =
            Needle(
                id = "callback-tool",
                name = "Callback Tool",
                description = "Tests callback invocation",
                code = "return 'success'",
                args = emptyList(),
                returnType = Needle.Arg.Type.String,
            )

        var callbackInvoked = false
        var capturedResult: Result<NeedleResult>? = null

        val adapter =
            NeedleToolAdapter(needle) { result ->
                callbackInvoked = true
                capturedResult = result
            }

        val args = NeedleToolAdapter.Args(JsonObject(emptyMap()))

        // When
        adapter.execute(args)

        // Then
        assertTrue(callbackInvoked, "Callback should be invoked")
        assertNotNull(capturedResult, "Result should be captured")
        assertTrue(capturedResult!!.isSuccess, "Result should be success")
        assertEquals("success", (capturedResult!!.getOrNull() as NeedleResult.StringResult).value)
    }

    @Test
    fun shouldHandleErrorsGracefully() = runTest {
        // Given
        val needle =
            Needle(
                id = "error-tool",
                name = "Error Tool",
                description = "Throws an error",
                code = "error('Test error')", // Lua error function
                args = emptyList(),
                returnType = Needle.Arg.Type.String,
            )

        val adapter = NeedleToolAdapter(needle)
        val args = NeedleToolAdapter.Args(JsonObject(emptyMap()))

        // When
        val result = adapter.execute(args)

        // Then
        assertTrue(result.startsWith("Error executing needle:"), "Should return error message")
    }

    @Test
    fun shouldInvokeCallbackOnError() = runTest {
        // Given
        val needle =
            Needle(
                id = "error-callback-tool",
                name = "Error Callback Tool",
                description = "Tests error callback",
                code = "error('Test error')",
                args = emptyList(),
                returnType = Needle.Arg.Type.String,
            )

        var callbackInvoked = false
        var capturedResult: Result<NeedleResult>? = null

        val adapter =
            NeedleToolAdapter(needle) { result ->
                callbackInvoked = true
                capturedResult = result
            }

        val args = NeedleToolAdapter.Args(JsonObject(emptyMap()))

        // When
        adapter.execute(args)

        // Then
        assertTrue(callbackInvoked, "Callback should be invoked even on error")
        assertNotNull(capturedResult, "Result should be captured")
        assertTrue(capturedResult!!.isFailure, "Result should be failure")
    }

    @Test
    fun shouldHandleBooleanArguments() = runTest {
        // Given
        val needle =
            Needle(
                id = "bool-tool",
                name = "Boolean Tool",
                description = "Returns boolean",
                code = "return flag",
                args =
                    listOf(
                        Needle.Arg(
                            name = "flag",
                            type = Needle.Arg.Type.Boolean,
                            description = "Boolean flag",
                        )
                    ),
                returnType = Needle.Arg.Type.Boolean,
            )

        val adapter = NeedleToolAdapter(needle)
        val args = NeedleToolAdapter.Args(JsonObject(mapOf("flag" to JsonPrimitive("true"))))

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("true", result)
    }

    @Test
    fun shouldHandleFloatArguments() = runTest {
        // Given
        val needle =
            Needle(
                id = "float-tool",
                name = "Float Tool",
                description = "Multiplies float",
                code = "return value * 2.5",
                args =
                    listOf(
                        Needle.Arg(
                            name = "value",
                            type = Needle.Arg.Type.Float,
                            description = "Float value",
                        )
                    ),
                returnType = Needle.Arg.Type.Float,
            )

        val adapter = NeedleToolAdapter(needle)
        val args = NeedleToolAdapter.Args(JsonObject(mapOf("value" to JsonPrimitive("10.0"))))

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("25.0", result)
    }

    @Test
    fun shouldHandleMissingOptionalArguments() = runTest {
        // Given
        val needle =
            Needle(
                id = "optional-tool",
                name = "Optional Tool",
                description = "Has optional args",
                code = "return 'ok'",
                args =
                    listOf(
                        Needle.Arg(
                            name = "optional",
                            type = Needle.Arg.Type.String,
                            description = "Optional param",
                        )
                    ),
                returnType = Needle.Arg.Type.String,
            )

        val adapter = NeedleToolAdapter(needle)
        val args = NeedleToolAdapter.Args(JsonObject(emptyMap())) // No arguments provided

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("ok", result) // Should still execute successfully
    }
}
