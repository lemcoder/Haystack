package io.github.lemcoder.needle.lua

import io.github.lemcoder.needle.lua.module.LoggingModule
import io.github.lemcoder.needle.lua.module.LuaLoggingModule
import io.github.lemcoder.needle.lua.module.LuaNetworkModule
import io.github.lemcoder.needle.lua.module.NetworkModule
import party.iroiro.luajava.lua55.Lua55

actual fun createLuaExecutor(): Executor {
    val lua = Lua55()
    val logModule = LuaLoggingModule(lua)
    val networkModule = LuaNetworkModule(lua)

    return AndroidExecutor(logModule, networkModule)
}

internal class AndroidExecutor(
    private val logModule: LoggingModule,
    private val networkModule: NetworkModule
) : Executor {

    override fun <OUT> run(code: String, args: Map<String, Any?>): OUT? {
        Lua55().use { lua ->
            lua.openLibraries()

            logModule.install()
            networkModule.install()

            // Inject arguments into Lua globals
            for ((key, value) in args) {
                if (value == null) continue
                lua.set(key, value)
            }

            // Wrap code so return works even if user forgets
            val wrappedCode = """
                return (function()
                    $code
                end)()
            """.trimIndent()

            val results = lua.eval(wrappedCode)

            if (results.isEmpty()) return null

            return results[0].toJavaObject() as? OUT ?: run {
                throw IllegalStateException("Unexpected return type: ${results[0].javaClass.name}")
            }
        }
    }
}