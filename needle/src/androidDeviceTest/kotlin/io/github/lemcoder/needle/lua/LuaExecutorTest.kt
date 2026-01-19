package io.github.lemcoder.needle.lua

import kotlin.test.Test
import kotlin.test.assertTrue

class LuaExecutorTest {
    @Test
    fun shouldPerformBasicAddition() {
        val executor = createLuaExecutor()
        val result: Double? = executor.run("return a + b", mapOf(
            "a" to 34,
            "b" to 35
        ))
        assertTrue { result == 69.0 }
    }

    @Test
    fun shouldCallHttpGet() {
        val executor = createLuaExecutor()

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
                    result.contains("\"url\": \"https://httpbin.org/get\"")
        )
    }

}