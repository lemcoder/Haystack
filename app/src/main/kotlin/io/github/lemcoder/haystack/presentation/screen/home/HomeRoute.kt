package io.github.lemcoder.haystack.presentation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.presentation.common.Route

@Composable
fun HomeRoute() = Route<Destination.Home> {
    val viewModel = viewModel { HomeViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(state = state, onEvent = viewModel::onEvent)
}
