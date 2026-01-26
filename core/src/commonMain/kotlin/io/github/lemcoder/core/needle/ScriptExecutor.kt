package io.github.lemcoder.core.needle

import io.github.lemcoder.core.needle.module.FileSystemModule
import io.github.lemcoder.core.needle.module.LoggingModule
import io.github.lemcoder.core.needle.module.NetworkModule
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.instantiateScriptEngine

interface ScriptExecutor {

    fun <OUT> run(code: String, args: Map<String, Any?> = emptyMap()): OUT?
}

expect fun createScriptExecutor(
    baseDirPath: String,
    engine: ScriptEngine = instantiateScriptEngine(),
    loggingModule: LoggingModule? = null,
    networkModule: NetworkModule? = null,
    fileSystemModule: FileSystemModule? = null,
): ScriptExecutor
