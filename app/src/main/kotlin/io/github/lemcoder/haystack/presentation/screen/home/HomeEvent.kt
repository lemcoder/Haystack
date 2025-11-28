package io.github.lemcoder.haystack.presentation.screen.home

sealed interface HomeEvent {
    data object GenerateChart : HomeEvent

    data object OpenSettings : HomeEvent
}
