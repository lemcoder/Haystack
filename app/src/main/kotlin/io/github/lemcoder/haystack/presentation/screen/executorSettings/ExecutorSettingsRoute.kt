package io.github.lemcoder.haystack.presentation.screen.executorSettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ExecutorSettingsRoute() {
    val viewModel = viewModel { ExecutorSettingsViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    ExecutorSettingsScreen(state = state, onEvent = viewModel::onEvent)
}
