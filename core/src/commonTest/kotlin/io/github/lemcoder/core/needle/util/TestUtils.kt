package io.github.lemcoder.core.needle.util

import io.github.lemcoder.core.needle.ScriptExecutor
import io.github.lemcoder.core.needle.createScriptExecutor
import io.github.lemcoder.core.needle.module.FileSystemModule
import io.github.lemcoder.core.needle.module.LoggingModule
import io.github.lemcoder.core.needle.module.NetworkModule
import io.github.lemcoder.core.needle.module.TestLuaFileSystemModule
import io.github.lemcoder.core.needle.module.TestLuaLoggingModule
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.instantiateScriptEngine
import kotlinx.coroutines.test.TestScope
import needle.module.TestLuaNetworkModule

fun TestScope.createTestScriptExecutor(
    engine: ScriptEngine = instantiateScriptEngine(),
    loggingModule: LoggingModule = TestLuaLoggingModule(engine),
    networkModule: NetworkModule = TestLuaNetworkModule(engine, this),
    fileSystemModule: FileSystemModule = TestLuaFileSystemModule(engine),
): ScriptExecutor = createScriptExecutor("", engine, loggingModule, networkModule, fileSystemModule)
