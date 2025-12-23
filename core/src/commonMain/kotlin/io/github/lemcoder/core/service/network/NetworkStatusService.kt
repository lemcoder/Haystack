package io.github.lemcoder.core.service.network

import kotlinx.coroutines.flow.Flow

interface NetworkStatusService {
    val isNetworkAvailable: Flow<Boolean>

    companion object {
        val Instance: NetworkStatusService by lazy { getNetworkStatusService() }
    }
}

internal expect fun getNetworkStatusService(): NetworkStatusService
