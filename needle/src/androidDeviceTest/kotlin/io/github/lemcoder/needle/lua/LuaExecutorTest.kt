package io.github.lemcoder.needle.lua

import androidx.test.runner.AndroidJUnitRunner
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertTrue

class LuaExecutorTest {
    @Test
    fun testRun() {
        val executor = createLuaExecutor()
        val result: Int? = executor.run("return 34 + 35", emptyMap())
        assertTrue { result == 69 }
    }
}