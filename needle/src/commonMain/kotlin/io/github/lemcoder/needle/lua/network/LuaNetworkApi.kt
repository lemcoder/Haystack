package io.github.lemcoder.needle.lua.network

import io.github.lemcoder.needle.util.HttpMethod
import io.github.lemcoder.needle.util.NetworkClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

internal class LuaNetworkApi(
    private val client: NetworkClient,
    private val scope: CoroutineScope
) {

    fun get(url: String): Map<String, Any?> =
        blockingRequest(HttpMethod.GET, url)

    fun post(url: String, body: String): Map<String, Any?> =
        blockingRequest(
            HttpMethod.POST,
            url,
            body = body.encodeToByteArray()
        )

    private fun blockingRequest(
        method: HttpMethod,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: ByteArray? = null
    ): Map<String, Any?> {

        val response = runBlocking(scope.coroutineContext) {
            client.request(method, url, headers, body)
        }

        return mapOf(
            "status" to response.statusCode,
            "body" to response.body.decodeToString(),
            "headers" to response.headers
        )
    }
}
