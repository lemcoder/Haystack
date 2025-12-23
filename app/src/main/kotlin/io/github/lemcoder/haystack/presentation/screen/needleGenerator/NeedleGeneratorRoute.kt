package io.github.lemcoder.haystack.presentation.screen.needleGenerator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NeedleGeneratorRoute() {
    val viewModel = viewModel { NeedleGeneratorViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    NeedleGeneratorScreen(state = state, onEvent = viewModel::onEvent)
}
