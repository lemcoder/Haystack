package io.github.lemcoder.needle.module

import io.github.lemcoder.needle.util.pushMap
import kotlinx.coroutines.CoroutineScope
import party.iroiro.luajava.AbstractLua

internal class TestLuaNetworkModule(
    private val lua: AbstractLua,
    private val scope: CoroutineScope
): NetworkModule {
    var status = 200
    var responseHeaders: Map<String, String> = mapOf(
        "Content-Type" to "application/json"
    )
    var responseBody: String = """{ "message": "Hello from TestLuaNetworkModule!" }"""

    /**
     * Helper object to expose network functions to Lua
     */
    private val networkApi = object {
        fun get(url: String) = this@TestLuaNetworkModule.get(url)
        fun post(url: String, body: String) = this@TestLuaNetworkModule.post(url, body)
    }

    override fun install() = with(lua) {
        push { lua ->
            val javaMap = lua.toJavaObject(1) as? Map<*, *>
                ?: throw IllegalArgumentException("Expected Map object")
            lua.pushMap(javaMap)
            1
        }
        setGlobal("__convertMapToTable")
        set("__network_api", networkApi)

        run(
            """
                network = {}
                function network:get(url)
                    local result = __network_api:get(url)
                    return __convertMapToTable(result)
                end
                function network:post(url, body)
                    local result = __network_api:post(url, body)
                    return __convertMapToTable(result)
                end
            """.trimIndent()
        )
    }

    override fun get(url: String) = blockingRequest()
    override fun post(url: String, body: String) = blockingRequest()

    private fun blockingRequest(): Map<String, Any?> {
        return mapOf(
            "status" to status,
            "headers" to responseHeaders,
            "body" to responseBody
        )
    }
}