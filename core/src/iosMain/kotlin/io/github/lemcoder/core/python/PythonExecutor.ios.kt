package io.github.lemcoder.core.python

internal actual fun getPlatformPythonExecutor(): PythonExecutor {
    return object : PythonExecutor {
        override fun execute(code: String, captureOutput: Boolean): String {
            TODO("Not yet implemented")
        }

        override fun executeSafe(
            code: String,
            captureOutput: Boolean
        ): Result<String> {
            TODO("Not yet implemented")
        }

        override fun <T> executeAndParse(
            code: String,
            parser: (String) -> T
        ): Result<T> {
            TODO("Not yet implemented")
        }

    }
}