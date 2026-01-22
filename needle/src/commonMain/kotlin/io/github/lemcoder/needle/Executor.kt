package io.github.lemcoder.needle

import io.github.lemcoder.needle.module.FileSystemModule
import io.github.lemcoder.needle.module.LoggingModule
import io.github.lemcoder.needle.module.NetworkModule
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.instantiateScriptEngine

interface Executor {

    fun <OUT> run(code: String, args: Map<String, Any?> = emptyMap()): OUT?
}

expect fun createExecutor(
    context: Any,
    engine: ScriptEngine = instantiateScriptEngine(),
    loggingModule: LoggingModule? = null,
    networkModule: NetworkModule? = null,
    fileSystemModule: FileSystemModule? = null,
): Executor
