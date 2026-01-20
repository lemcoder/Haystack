package io.github.lemcoder.needle

actual fun createLuaExecutor(context: Any): Executor {
    return object : Executor {
        override fun <OUT> run(
            code: String,
            args: Map<String, Any?>
        ): OUT? {
            TODO("Not yet implemented")
        }
    }
}