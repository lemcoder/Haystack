package io.github.lemcoder.needle.util

import io.github.lemcoder.needle.AndroidExecutor
import io.github.lemcoder.needle.Executor
import io.github.lemcoder.needle.module.FileSystemModule
import io.github.lemcoder.needle.module.LoggingModule
import io.github.lemcoder.needle.module.NetworkModule
import io.github.lemcoder.needle.module.TestLuaFileSystemModule
import io.github.lemcoder.needle.module.TestLuaLoggingModule
import io.github.lemcoder.needle.module.TestLuaNetworkModule
import kotlinx.coroutines.test.TestScope
import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.lua55.Lua55

fun TestScope.createTestLuaExecutor(
    lua: AbstractLua = Lua55(),
    loggingModule: LoggingModule = TestLuaLoggingModule(lua),
    networkModule: NetworkModule = TestLuaNetworkModule(lua, this),
    fileSystemModule: FileSystemModule = TestLuaFileSystemModule(lua),
): Executor = AndroidExecutor(lua, loggingModule, networkModule, fileSystemModule)
