package io.github.lemcoder.needle.lua

actual fun createLuaExecutor(): LuaExecutor {
    return object : LuaExecutor {
        override fun <OUT> run(
            code: String,
            args: Map<String, Any?>
        ): OUT? {
            TODO("Not yet implemented")
        }
    }
}