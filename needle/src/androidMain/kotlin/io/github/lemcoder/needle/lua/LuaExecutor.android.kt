package io.github.lemcoder.needle.lua

import io.github.lemcoder.needle.lua.network.LuaNetworkApi
import io.github.lemcoder.needle.util.NetworkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import party.iroiro.luajava.lua55.Lua55

actual fun createLuaExecutor(): LuaExecutor {
    return AndroidLuaExecutor()
}

internal class AndroidLuaExecutor : LuaExecutor {
    private val networkApi = LuaNetworkApi(
        client = NetworkClient.Instance,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    )

    override fun <OUT> run(code: String, args: Map<String, Any?>): OUT? {
        Lua55().use { lua ->
            lua.openLibraries()
            lua.set("network", networkApi)
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