package io.github.lemcoder.haystack.presentation.screen.needleGenerator

data class NeedleGeneratorState(
    val functionalityDescription: String = "",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val generatedNeedleName: String? = null,
)
