package io.github.lemcoder.needle.util

internal interface NetworkClient {
    suspend fun request(
        method: HttpMethod,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: ByteArray? = null,
        timeoutMs: Long = 10_000
    ): NetworkResponse

    companion object {
        val Instance: NetworkClient = createNetworkClient()
    }
}

internal enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH
}

internal data class NetworkResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as NetworkResponse

        if (statusCode != other.statusCode) return false
        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + headers.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }
}

internal expect fun createNetworkClient(): NetworkClient