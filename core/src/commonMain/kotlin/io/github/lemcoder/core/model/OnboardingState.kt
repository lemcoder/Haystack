package io.github.lemcoder.core.model

data class OnboardingState(
    val didAddSampleNeedles: Boolean = false
) {
    val isOnboardingComplete: Boolean
        get() = didAddSampleNeedles
}
