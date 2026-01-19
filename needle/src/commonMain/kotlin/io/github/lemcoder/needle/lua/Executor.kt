package io.github.lemcoder.needle.lua

interface Executor {

    fun<OUT> run(code: String, args: Map<String, Any?> = emptyMap()): OUT?
}

expect fun createLuaExecutor(): Executor