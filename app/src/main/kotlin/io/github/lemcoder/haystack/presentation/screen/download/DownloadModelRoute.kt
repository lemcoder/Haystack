package io.github.lemcoder.haystack.presentation.screen.download

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DownloadModelRoute() {
    val viewModel = viewModel { DownloadModelViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    DownloadModelScreen(state = state, onEvent = viewModel::onEvent)
}
