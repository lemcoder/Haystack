package io.github.lemcoder.needle

import android.content.Context
import io.github.lemcoder.needle.module.FileSystemModule
import io.github.lemcoder.needle.module.LoggingModule
import io.github.lemcoder.needle.module.LuaFileSystemModule
import io.github.lemcoder.needle.module.LuaLoggingModule
import io.github.lemcoder.needle.module.LuaNetworkModule
import io.github.lemcoder.needle.module.NetworkModule
import kotlin.collections.iterator
import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.lua55.Lua55

actual fun createLuaExecutor(context: Any): Executor {
    context as Context

    val lua = Lua55()
    val logModule = LuaLoggingModule(lua)
    val networkModule = LuaNetworkModule(lua)
    val fileSystemModule = LuaFileSystemModule(lua, context.filesDir)

    return AndroidExecutor(lua, logModule, networkModule, fileSystemModule)
}

internal class AndroidExecutor(
    private val lua: AbstractLua,
    private val logModule: LoggingModule,
    private val networkModule: NetworkModule,
    private val fileSystemModule: FileSystemModule,
) : Executor {

    override fun <OUT> run(code: String, args: Map<String, Any?>): OUT? {
        lua.use { lua ->
            lua.openLibraries()

            logModule.install()
            networkModule.install()
            fileSystemModule.install()

            // Inject arguments into Lua globals
            for ((key, value) in args) {
                if (value == null) continue
                lua.set(key, value)
            }

            // Wrap code so return works even if user forgets
            val wrappedCode =
                """
                return (function()
                    $code
                end)()
            """
                    .trimIndent()

            val results = lua.eval(wrappedCode)

            if (results.isEmpty()) return null

            return results[0].toJavaObject() as? OUT
                ?: run {
                    throw IllegalStateException(
                        "Unexpected return type: ${results[0].javaClass.name}"
                    )
                }
        }
    }
}
