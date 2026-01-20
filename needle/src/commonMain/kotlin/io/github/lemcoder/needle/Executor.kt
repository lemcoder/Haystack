package io.github.lemcoder.needle

interface Executor {

    fun<OUT> run(code: String, args: Map<String, Any?> = emptyMap()): OUT?
}

expect fun createLuaExecutor(context: Any): Executor