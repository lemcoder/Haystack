package io.github.lemcoder.haystack.presentation.screen.home

sealed interface HomeEvent {
    data object GenerateChart : HomeEvent
    // TODO: Add more events as needed
}
