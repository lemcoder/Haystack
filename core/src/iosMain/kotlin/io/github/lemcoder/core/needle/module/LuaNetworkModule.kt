package io.github.lemcoder.core.needle.module

import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.asString
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import platform.Foundation.*
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
internal class LuaNetworkModule(
    private val engine: ScriptEngine,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
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

    private fun request(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: ByteArray?,
        timeoutMs: Long = 10_000L,
    ): Map<String, Any?> {
        val nsUrl = NSURL.URLWithString(url) ?: throw IllegalArgumentException("Invalid URL: $url")

        val request =
            NSMutableURLRequest.requestWithURL(nsUrl).apply {
                setHTTPMethod(method)
                setTimeoutInterval(timeoutMs / 1000.0)

                headers.forEach { (key, value) -> setValue(value, forHTTPHeaderField = key) }

                if (body != null) {
                    setHTTPBody(body.toNSData())
                }
            }

        var responseData: NSData? = null
        var responseError: NSError? = null
        var httpResponse: NSHTTPURLResponse? = null
        var completed = false

        val session = NSURLSession.sharedSession()
        val task =
            session.dataTaskWithRequest(request) { data, response, error ->
                responseData = data
                responseError = error
                httpResponse = response as? NSHTTPURLResponse
                completed = true
            }

        task.resume()

        // Wait for completion
        while (!completed) {
            NSRunLoop.currentRunLoop()
                .runMode(
                    NSDefaultRunLoopMode,
                    beforeDate = NSDate.dateWithTimeIntervalSinceNow(0.1),
                )
        }

        if (responseError != null) {
            throw Exception("Network request failed: ${responseError?.localizedDescription}")
        }

        val status = httpResponse?.statusCode?.toInt() ?: 0
        val responseHeaders =
            httpResponse?.allHeaderFields?.let { headers ->
                headers.entries.associate { (it.key.toString()) to (it.value.toString()) }
            } ?: emptyMap()

        val responseBody = responseData?.toByteArray()?.decodeToString() ?: ""

        return mapOf("status" to status, "body" to responseBody, "headers" to responseHeaders)
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

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), this.size.toULong())
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun NSData.toByteArray(): ByteArray {
    return ByteArray(this.length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}
