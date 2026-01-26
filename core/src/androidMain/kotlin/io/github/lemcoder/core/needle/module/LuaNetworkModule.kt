package io.github.lemcoder.core.needle.module

import io.github.lemcoder.core.needle.module.NetworkModule
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.asString
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

internal class LuaNetworkModule(
    private val engine: ScriptEngine,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : NetworkModule {

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
            """
                .trimIndent()
        )
    }

    override fun get(url: String): Map<String, Any?> = blockingRequest("GET", url)

    override fun post(url: String, body: String): Map<String, Any?> =
        blockingRequest("POST", url, body = body.encodeToByteArray())

    private fun blockingRequest(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: ByteArray? = null,
    ): Map<String, Any?> =
        runBlocking(scope.coroutineContext) { request(method, url, headers, body) }

    private suspend fun request(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: ByteArray?,
        timeoutMs: Long = 10_000L,
    ): Map<String, Any?> =
        withContext(Dispatchers.IO) {
            val connection =
                (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    connectTimeout = timeoutMs.toInt()
                    readTimeout = timeoutMs.toInt()
                    doInput = true

                    headers.forEach { (k, v) -> setRequestProperty(k, v) }

                    if (body != null) {
                        doOutput = true
                        outputStream.use { it.write(body) }
                    }
                }

            val status = connection.responseCode
            val responseHeaders = connection.headerFields.filterKeys { it != null }

            val stream =
                if (status in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val responseBody = stream?.readBytes() ?: ByteArray(0)

            mapOf(
                "status" to status,
                "body" to responseBody.decodeToString(),
                "headers" to responseHeaders,
            )
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
                        .associate { (k, v) -> k as String to toScriptValue(v) }
                )
            is List<*> -> ScriptValue.ListVal(value.map { toScriptValue(it) })
            else -> ScriptValue.Str(value.toString())
        }
}
