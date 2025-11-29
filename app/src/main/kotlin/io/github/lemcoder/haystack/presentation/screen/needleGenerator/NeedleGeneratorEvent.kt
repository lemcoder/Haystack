package io.github.lemcoder.haystack.presentation.screen.needleGenerator

sealed interface NeedleGeneratorEvent {
    data class UpdateFunctionalityDescription(val value: String) : NeedleGeneratorEvent
    data object GenerateNeedle : NeedleGeneratorEvent
    data object ClearError : NeedleGeneratorEvent
    data object NavigateBack : NeedleGeneratorEvent
}
