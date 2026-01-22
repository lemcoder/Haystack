package io.github.lemcoder.needle

interface Executor {

    fun <OUT> run(code: String, args: Map<String, Any?> = emptyMap()): OUT?
}

expect fun createExecutor(context: Any): Executor
