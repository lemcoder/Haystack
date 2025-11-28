package io.github.lemcoder.haystack.core.model

data class OnboardingState(
    val didAddSampleNeedles: Boolean = false
) {
    val isOnboardingComplete: Boolean
        get() = didAddSampleNeedles
}
