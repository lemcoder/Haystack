package io.github.lemcoder.needle.lua

import android.util.Log
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class LuaExecutorTest {
    @Test
    fun shouldPerformBasicAddition() {
        val executor = createLuaExecutor()
        val result: Double? = executor.run(
            "return a + b", mapOf(
                "a" to 34,
                "b" to 35
            )
        )
        assertTrue { result == 69.0 }
    }

    @Test
    fun testLuaTables() {
        val executor = createLuaExecutor()
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