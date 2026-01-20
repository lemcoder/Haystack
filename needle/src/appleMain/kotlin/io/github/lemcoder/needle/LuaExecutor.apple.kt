package io.github.lemcoder.needle

actual fun createLuaExecutor(): Executor {
    return object : Executor {
        override fun <OUT> run(
            code: String,
            args: Map<String, Any?>
        ): OUT? {
            TODO("Not yet implemented")
        }
    }
}