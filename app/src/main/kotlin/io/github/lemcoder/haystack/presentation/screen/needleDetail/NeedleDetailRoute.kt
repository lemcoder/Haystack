package io.github.lemcoder.haystack.presentation.screen.needleDetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.presentation.common.Route

@Composable
fun NeedleDetailRoute() =
    Route<Destination.NeedleDetail> { key ->
        val needleId = key.needleId
        val viewModel: NeedleDetailViewModel =
            viewModel(
                key = needleId // Use needleId as key so ViewModel recreates for each needle
            ) {
                NeedleDetailViewModel(needleId = needleId)
            }
        val state by viewModel.state.collectAsState()

        NeedleDetailScreen(state = state, onEvent = viewModel::onEvent)
    }
