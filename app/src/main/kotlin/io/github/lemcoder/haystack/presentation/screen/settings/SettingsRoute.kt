package io.github.lemcoder.haystack.presentation.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsRoute() {
    val viewModel = viewModel { SettingsViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(state = state, onEvent = viewModel::onEvent)
}
