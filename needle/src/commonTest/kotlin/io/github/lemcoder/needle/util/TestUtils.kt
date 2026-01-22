package io.github.lemcoder.needle.util

import io.github.lemcoder.needle.Executor
import io.github.lemcoder.needle.createExecutor
import io.github.lemcoder.needle.module.FileSystemModule
import io.github.lemcoder.needle.module.LoggingModule
import io.github.lemcoder.needle.module.NetworkModule
import io.github.lemcoder.needle.module.TestLuaFileSystemModule
import io.github.lemcoder.needle.module.TestLuaLoggingModule
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.instantiateScriptEngine
import kotlinx.coroutines.test.TestScope
import needle.module.TestLuaNetworkModule

fun TestScope.createTestScriptExecutor(
    engine: ScriptEngine = instantiateScriptEngine(),
    loggingModule: LoggingModule = TestLuaLoggingModule(engine),
    networkModule: NetworkModule = TestLuaNetworkModule(engine, this),
    fileSystemModule: FileSystemModule = TestLuaFileSystemModule(engine),
): Executor = createExecutor(Any(), engine, loggingModule, networkModule, fileSystemModule)
