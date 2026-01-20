package io.github.lemcoder.needle.module

import io.github.lemcoder.needle.util.pushMap
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import party.iroiro.luajava.AbstractLua

internal class LuaNetworkModule(
    private val lua: AbstractLua,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : NetworkModule {
    /** Helper object to expose network functions to Lua */
    private val networkApi =
        object {
            fun get(url: String) = this@LuaNetworkModule.get(url)

            fun post(url: String, body: String) = this@LuaNetworkModule.post(url, body)
        }

    override fun install() =
        with(lua) {
            push { lua ->
                val javaMap =
                    lua.toJavaObject(1) as? Map<*, *>
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
                """
                    .trimIndent()
            )
        }

    override fun get(url: String) = blockingRequest("GET", url)

    override fun post(url: String, body: String) =
        blockingRequest("POST", url, body = body.encodeToByteArray())

    private fun blockingRequest(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: ByteArray? = null,
    ): Map<String, Any?> =
        runBlocking(scope.coroutineContext) {
            return@runBlocking request(method, url, headers, body)
        }

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
}
