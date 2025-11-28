package io.github.lemcoder.haystack.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.lemcoder.haystack.core.model.OnboardingState
import io.github.lemcoder.haystack.core.service.OnboardingService

/**
 * Provides the current onboarding state as a composable.
 * This can be used in any screen to access the onboarding status.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     val onboardingState = rememberOnboardingState()
 *
 *     if (!onboardingState.isOnboardingComplete) {
 *         // Show onboarding UI or trigger onboarding actions
 *     }
 * }
 * ```
 */
@Composable
fun rememberOnboardingState(): OnboardingState {
    val onboardingState by OnboardingService.Instance
        .onboardingState
        .collectAsStateWithLifecycle(initialValue = OnboardingState())

    return onboardingState
}
