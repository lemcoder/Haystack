package io.github.lemcoder.haystack.presentation.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class MviViewModel<STATE, EVENT> : ViewModel() {
    abstract val state: StateFlow<STATE>

    abstract fun onEvent(event: EVENT)
}
