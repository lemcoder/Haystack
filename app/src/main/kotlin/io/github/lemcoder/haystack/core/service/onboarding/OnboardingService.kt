package io.github.lemcoder.haystack.core.service.onboarding

import io.github.lemcoder.haystack.core.data.OnboardingRepository
import io.github.lemcoder.haystack.core.model.OnboardingState
import kotlinx.coroutines.flow.Flow

interface OnboardingService {
    val onboardingState: Flow<OnboardingState>
    suspend fun markSampleNeedlesAdded()

    companion object {
        val Instance: OnboardingService by lazy {
            OnboardingServiceImpl()
        }
    }
}

internal class OnboardingServiceImpl(
    private val onboardingRepository: OnboardingRepository = OnboardingRepository.Instance
) : OnboardingService {

    override val onboardingState: Flow<OnboardingState> =
        onboardingRepository.onboardingStateFlow

    override suspend fun markSampleNeedlesAdded() {
        onboardingRepository.markSampleNeedlesAdded()
    }
}
