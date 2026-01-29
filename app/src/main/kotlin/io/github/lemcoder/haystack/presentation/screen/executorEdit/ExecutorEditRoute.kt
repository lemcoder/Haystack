package io.github.lemcoder.haystack.presentation.screen.executorEdit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.presentation.common.Route

@Composable
fun ExecutorEditRoute() = Route<Destination.ExecutorEdit> { key ->
    val executorType = key.executorType
    val viewModel = viewModel { ExecutorEditViewModel(executorType = executorType) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    ExecutorEditScreen(state = state, onEvent = viewModel::onEvent)
}
