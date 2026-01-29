package io.github.lemcoder.haystack.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object SnackbarUtil {
    lateinit var snackbarHostState: SnackbarHostState

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        scope.launch {
            snackbarHostState.showSnackbar(message, actionLabel, duration = duration)
        }
    }
}
