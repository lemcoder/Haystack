package io.github.lemcoder.needle.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

internal actual fun createNetworkClient(): NetworkClient = AndroidNetworkClient()

private class AndroidNetworkClient : NetworkClient {

    override suspend fun request(
        method: HttpMethod,
        url: String,
        headers: Map<String, String>,
        body: ByteArray?,
        timeoutMs: Long
    ): NetworkResponse = withContext(Dispatchers.IO) {

        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method.name
            connectTimeout = timeoutMs.toInt()
            readTimeout = timeoutMs.toInt()
            doInput = true

            headers.forEach { (k, v) ->
                setRequestProperty(k, v)
            }

            if (body != null) {
                doOutput = true
                outputStream.use { it.write(body) }
            }
        }

        val status = connection.responseCode
        val responseHeaders = connection.headerFields.filterKeys { it != null }

        val stream = if (status in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseBody = stream?.readBytes() ?: ByteArray(0)

        NetworkResponse(
            statusCode = status,
            headers = responseHeaders,
            body = responseBody
        )
    }
}
