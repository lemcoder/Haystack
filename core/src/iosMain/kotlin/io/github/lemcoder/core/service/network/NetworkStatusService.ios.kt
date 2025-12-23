package io.github.lemcoder.core.service.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.*
import platform.darwin.dispatch_get_main_queue

internal actual fun getNetworkStatusService(): NetworkStatusService =
    NetworkStatusServiceImpl()

internal class NetworkStatusServiceImpl : NetworkStatusService {

    override val isNetworkAvailable: Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()

        nw_path_monitor_set_update_handler(monitor) { path ->
            val isAvailable =
                nw_path_get_status(path) == nw_path_status_satisfied
            trySend(isAvailable)
        }

        nw_path_monitor_set_queue(
            monitor,
            dispatch_get_main_queue()
        )

        nw_path_monitor_start(monitor)

        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }
}

