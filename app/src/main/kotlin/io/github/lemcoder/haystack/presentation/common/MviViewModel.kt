package io.github.lemcoder.haystack.presentation.common

import androidx.lifecycle.ViewModel
import io.github.lemcoder.core.utils.Log
import kotlinx.coroutines.flow.StateFlow

abstract class MviViewModel<STATE, EVENT> : ViewModel() {
    val TAG: String = this::class.java.simpleName

    init {
        Log.d(TAG, "Initialized")
    }
    abstract val state: StateFlow<STATE>

    abstract fun onEvent(event: EVENT)

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "Cleared")
    }
}
