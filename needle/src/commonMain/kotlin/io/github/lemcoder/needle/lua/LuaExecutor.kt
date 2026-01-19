package io.github.lemcoder.needle.lua

interface LuaExecutor {

    fun<OUT> run(code: String, args: Map<String, Any?>): OUT?
}

expect fun createLuaExecutor(): LuaExecutor