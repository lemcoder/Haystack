package io.github.lemcoder.needle.module

import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.asString
import kotlinx.coroutines.CoroutineScope

internal class TestLuaNetworkModule(
    private val engine: ScriptEngine,
    private val scope: CoroutineScope,
) : NetworkModule {
    var status = 200
    var responseHeaders: Map<String, String> = mapOf("Content-Type" to "application/json")
    var responseBody: String = """{ "message": "Hello from TestLuaNetworkModule!" }"""

    override fun install() {
        engine.registerFunction("__net_get") { args ->
            val url = args[0].asString()
            val result = get(url)
            ScriptValue.MapVal(result.mapValues { toScriptValue(it.value) })
        }

        engine.registerFunction("__net_post") { args ->
            val url = args[0].asString()
            val body = args[1].asString()
            val result = post(url, body)
            ScriptValue.MapVal(result.mapValues { toScriptValue(it.value) })
        }

        engine.eval(
            """
            network = {}
            function network:get(url)
                return __net_get(url)
            end
            function network:post(url, body)
                return __net_post(url, body)
            end
            """.trimIndent()
        )
    }


    override fun get(url: String) = blockingRequest()

    override fun post(url: String, body: String) = blockingRequest()

    private fun blockingRequest(): Map<String, Any?> {
        return mapOf("status" to status, "headers" to responseHeaders, "body" to responseBody)
    }

    private fun toScriptValue(value: Any?): ScriptValue =
        when (value) {
            null -> ScriptValue.Nil
            is String -> ScriptValue.Str(value)
            is Number -> ScriptValue.Num(value.toDouble())
            is Boolean -> ScriptValue.Bool(value)
            is Map<*, *> ->
                ScriptValue.MapVal(
                    value.entries
                        .filter { it.key is String }
                        .associate { (k, v) ->
                            k as String to toScriptValue(v)
                        }
                )
            is List<*> ->
                ScriptValue.ListVal(value.map { toScriptValue(it) })
            else -> ScriptValue.Str(value.toString())
        }
}
