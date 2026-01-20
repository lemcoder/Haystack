package io.github.lemcoder.needle

import android.util.Log
import io.github.lemcoder.needle.module.TestLuaLoggingModule
import io.github.lemcoder.needle.module.TestLuaNetworkModule
import io.github.lemcoder.needle.util.createTestLuaExecutor
import kotlinx.coroutines.test.runTest
import party.iroiro.luajava.lua55.Lua55
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LuaExecutorTest {

    @Test
    fun shouldPerformBasicAddition() = runTest {
        val executor = createTestLuaExecutor()
        val result: Double? = executor.run(
            "return a + b", mapOf(
                "a" to 34,
                "b" to 35
            )
        )
        assertTrue { result == 69.0 }
    }

    @Test
    fun testLuaTables() = runTest {
        val executor = createTestLuaExecutor()
        val table: Map<String, *> =
            executor.run("return { text = 'abc', children = { 'a', 'b', 'c' } }")!!
        assertEquals("abc", table["text"].toString())
        val children: MutableMap<Double, String> = table["children"] as MutableMap<Double, String>
        // Indices are 1-based.
        Log.e("LuaExecutorTest", "Children: $children")
        assertEquals("a", children[1.0].toString())
        assertEquals(3, children.size)
    }

    @Test
    fun shouldCallHttpGet() = runTest {
        val lua = Lua55()
        val networkModule = TestLuaNetworkModule(lua, this)
        networkModule.responseBody = "TEST"
        
        val executor = createTestLuaExecutor(
            lua = lua,
            networkModule = networkModule
        )

        val result: String? = executor.run(
            """
        local response = network:get("https://httpbin.org/get")
        for k, v in pairs(response) do
            print(k, type(v))
        end
        if response.status == 200 then
            return response.body
        else
            return "error: " .. response.status
        end
        """.trimIndent(),
            emptyMap()
        )

        assertTrue(
            result != null &&
                    result.contains("TEST")
        )
    }

    @Test
    fun shouldCallHttpPost() = runTest {
        val lua = Lua55()
        val networkModule = TestLuaNetworkModule(lua, this)
        networkModule.status = 201
        networkModule.responseBody = """{"id": 123, "created": true}"""
        
        val executor = createTestLuaExecutor(
            lua = lua,
            networkModule = networkModule
        )

        val result: String? = executor.run(
            """
        local requestBody = '{"name": "John Doe", "email": "john@example.com"}'
        local response = network:post("https://httpbin.org/post", requestBody)
        
        if response.status == 201 then
            return response.body
        else
            return "error: " .. response.status
        end
        """.trimIndent(),
            emptyMap()
        )

        assertTrue(
            result != null &&
                    result.contains("\"id\": 123") &&
                    result.contains("\"created\": true")
        )
    }

    @Test
    fun shouldCallLoggingFunctions() = runTest {
        val lua = Lua55()
        val loggingModule = TestLuaLoggingModule(lua)
        
        var debugCalled = false
        var infoCalled = false
        var warnCalled = false
        var errorCalled = false
        
        loggingModule.onDebugCalled = { tag, message ->
            assertEquals("TestTag", tag)
            assertEquals("Debug message", message)
            debugCalled = true
        }
        
        loggingModule.onInfoCalled = { tag, message ->
            assertEquals("TestTag", tag)
            assertEquals("Info message", message)
            infoCalled = true
        }
        
        loggingModule.onWarnCalled = { tag, message ->
            assertEquals("TestTag", tag)
            assertEquals("Warning message", message)
            warnCalled = true
        }
        
        loggingModule.onErrorCalled = { tag, message ->
            assertEquals("TestTag", tag)
            assertEquals("Error message", message)
            errorCalled = true
        }
        
        val executor = createTestLuaExecutor(
            lua = lua,
            loggingModule = loggingModule
        )

        executor.run<Unit>(
            """
        log:d("TestTag", "Debug message")
        log:i("TestTag", "Info message")
        log:w("TestTag", "Warning message")
        log:e("TestTag", "Error message")
        """.trimIndent(),
            emptyMap()
        )

        assertTrue(debugCalled, "Debug logging function was not called")
        assertTrue(infoCalled, "Info logging function was not called")
        assertTrue(warnCalled, "Warning logging function was not called")
        assertTrue(errorCalled, "Error logging function was not called")
    }
}