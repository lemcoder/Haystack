package io.github.lemcoder.needle.module

import io.github.lemcoder.lua.Lua
import io.github.lemcoder.lua.value.LuaFunction
import io.github.lemcoder.lua.value.LuaValue
import io.github.lemcoder.needle.util.pushMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal class TestLuaNetworkModule(
    private val lua: Lua,
    private val scope: CoroutineScope,
) : NetworkModule {
    var status = 200
    var responseHeaders: Map<String, String> = mapOf("Content-Type" to "application/json")
    var responseBody: String = """{ "message": "Hello from TestLuaNetworkModule!" }"""

    /** Helper object to expose network functions to Lua */
    private val networkApi =
        object {
            fun get(url: String) = this@TestLuaNetworkModule.get(url)

            fun post(url: String, body: String) = this@TestLuaNetworkModule.post(url, body)
        }

    private val convertMapToTable: LuaFunction = object : LuaFunction {
        override fun call(L: Lua?, args: List<LuaValue?>?): List<LuaValue?> {
            val obj = args?.getOrNull(0)?.toJavaObject()
            val javaMap = when (obj) {
                is Map<*, *> -> obj
                is String -> {
                    Json.parseToJsonElement(obj)
                        .jsonObject
                        .mapValues { it.value.toString() }
                }
                else -> throw IllegalArgumentException("Expected Map or JSON string, got ${obj?.javaClass}")
            }

            lua.pushMap(javaMap)
            return listOf(lua.get())
        }
    }

    override fun install() = with(lua) {
        register("__convertMapToTable", convertMapToTable)
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
        return mapOf("status" to status, "headers" to responseHeaders, "body" to responseBody)
    }
}
