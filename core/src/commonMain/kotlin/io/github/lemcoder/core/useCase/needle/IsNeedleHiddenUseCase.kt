package io.github.lemcoder.core.useCase.needle

import io.github.lemcoder.core.data.repository.NeedleRepository

interface IsNeedleHiddenUseCase {
    suspend operator fun invoke(needleId: String): Boolean

    companion object {
        fun create(): IsNeedleHiddenUseCase {
            return IsNeedleHiddenUseCaseImpl()
        }
    }
}

private class IsNeedleHiddenUseCaseImpl(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) : IsNeedleHiddenUseCase {
    override suspend fun invoke(needleId: String): Boolean {
        return needleRepository.isNeedleHidden(needleId)
    }
}
