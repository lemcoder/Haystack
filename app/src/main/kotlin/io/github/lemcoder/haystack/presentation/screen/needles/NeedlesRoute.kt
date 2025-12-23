package io.github.lemcoder.haystack.presentation.screen.needles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NeedlesRoute() {
    val viewModel = viewModel { NeedlesViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    NeedlesScreen(state = state, onEvent = viewModel::onEvent)
}
