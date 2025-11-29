package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.repository.NeedleRepository

class IsNeedleHiddenUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    suspend operator fun invoke(needleId: String): Boolean {
        return needleRepository.isNeedleHidden(needleId)
    }
}
