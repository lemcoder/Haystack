package io.github.lemcoder.haystack.presentation.screen.needles

import io.github.lemcoder.haystack.presentation.common.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NeedlesViewModel : MviViewModel<NeedlesState, NeedlesEvent>() {
    private val _state = MutableStateFlow(NeedlesState())
    override val state: StateFlow<NeedlesState> = _state.asStateFlow()

    override fun onEvent(event: NeedlesEvent) {
        // TODO: Handle events
    }
}
