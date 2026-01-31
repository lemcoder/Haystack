package io.github.lemcoder.core.koog

import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult
import io.github.lemcoder.core.needle.NeedleParameter
import io.github.lemcoder.core.needle.NeedleToolExecutor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class NeedleToolAdapterTest {

    // Mock executor that returns predictable results for testing
    private class MockNeedleToolExecutor : NeedleToolExecutor(null) {
        var lastNeedle: Needle? = null
        var lastParams: List<NeedleParameter>? = null
        var mockResult: Result<NeedleResult> = Result.success(NeedleResult.StringResult("mock"))

        override fun executeNeedle(
            needle: Needle,
            params: List<NeedleParameter>,
        ): Result<NeedleResult> {
            lastNeedle = needle
            lastParams = params
            return mockResult
        }
    }

    @Test
    fun shouldCreateToolWithCorrectName() {
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
        val mockExecutor = MockNeedleToolExecutor()

        // When
        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)

        // Then - Verify adapter is created with correct name
        assertEquals("test-tool", adapter.name)
    }

    @Test
    fun shouldExecuteNeedleAndReturnResult() = runTest {
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

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.success(NeedleResult.StringResult("Hello World"))

        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)
        val args = NeedleToolAdapter.Args(JsonObject(emptyMap()))

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("Hello World", result)
        assertEquals(needle, mockExecutor.lastNeedle)
    }

    @Test
    fun shouldParseStringArgument() = runTest {
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

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.success(NeedleResult.StringResult("Hello from test"))

        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)
        val args =
            NeedleToolAdapter.Args(JsonObject(mapOf("message" to JsonPrimitive("Hello from test"))))

        // When
        adapter.execute(args)

        // Then - Verify parameter was parsed correctly
        val params = mockExecutor.lastParams!!
        assertEquals(1, params.size)
        assertEquals("message", params[0].name)
        assertEquals("Hello from test", (params[0] as NeedleParameter.StringParam).value)
    }

    @Test
    fun shouldParseIntArgument() = runTest {
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

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.success(NeedleResult.IntResult(42))

        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)
        val args = NeedleToolAdapter.Args(JsonObject(mapOf("value" to JsonPrimitive("21"))))

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("42", result)
        val params = mockExecutor.lastParams!!
        assertEquals(21, (params[0] as NeedleParameter.IntParam).value)
    }

    @Test
    fun shouldParseMultipleArguments() = runTest {
        // Given
        val needle =
            Needle(
                id = "add-tool",
                name = "Add Tool",
                description = "Adds two numbers",
                code = "return a + b",
                args =
                    listOf(
                        Needle.Arg(
                            name = "a",
                            type = Needle.Arg.Type.Int,
                            description = "First number",
                        ),
                        Needle.Arg(
                            name = "b",
                            type = Needle.Arg.Type.Int,
                            description = "Second number",
                        ),
                    ),
                returnType = Needle.Arg.Type.Int,
            )

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.success(NeedleResult.IntResult(42))

        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)
        val args =
            NeedleToolAdapter.Args(
                JsonObject(mapOf("a" to JsonPrimitive("10"), "b" to JsonPrimitive("32")))
            )

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("42", result)
        val params = mockExecutor.lastParams!!
        assertEquals(2, params.size)
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

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.success(NeedleResult.StringResult("success"))

        val adapter =
            NeedleToolAdapter(
                needle,
                onNeedleResult = { result ->
                    callbackInvoked = true
                    capturedResult = result
                },
                needleExecutor = mockExecutor,
            )

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
                code = "error('Test error')",
                args = emptyList(),
                returnType = Needle.Arg.Type.String,
            )

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.failure(RuntimeException("Test error"))

        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)
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

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.failure(RuntimeException("Test error"))

        val adapter =
            NeedleToolAdapter(
                needle,
                onNeedleResult = { result ->
                    callbackInvoked = true
                    capturedResult = result
                },
                needleExecutor = mockExecutor,
            )

        val args = NeedleToolAdapter.Args(JsonObject(emptyMap()))

        // When
        adapter.execute(args)

        // Then
        assertTrue(callbackInvoked, "Callback should be invoked even on error")
        assertNotNull(capturedResult, "Result should be captured")
        assertTrue(capturedResult!!.isFailure, "Result should be failure")
    }

    @Test
    fun shouldParseBooleanArguments() = runTest {
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

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.success(NeedleResult.BooleanResult(true))

        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)
        val args = NeedleToolAdapter.Args(JsonObject(mapOf("flag" to JsonPrimitive("true"))))

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("true", result)
        val params = mockExecutor.lastParams!!
        assertEquals(true, (params[0] as NeedleParameter.BooleanParam).value)
    }

    @Test
    fun shouldParseFloatArguments() = runTest {
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

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.success(NeedleResult.FloatResult(25.0f))

        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)
        val args = NeedleToolAdapter.Args(JsonObject(mapOf("value" to JsonPrimitive("10.0"))))

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("25.0", result)
        val params = mockExecutor.lastParams!!
        assertEquals(10.0f, (params[0] as NeedleParameter.FloatParam).value)
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

        val mockExecutor = MockNeedleToolExecutor()
        mockExecutor.mockResult = Result.success(NeedleResult.StringResult("ok"))

        val adapter = NeedleToolAdapter(needle, needleExecutor = mockExecutor)
        val args = NeedleToolAdapter.Args(JsonObject(emptyMap())) // No arguments provided

        // When
        val result = adapter.execute(args)

        // Then
        assertEquals("ok", result)
        // No parameters should be passed
        val params = mockExecutor.lastParams!!
        assertEquals(0, params.size)
    }
}
