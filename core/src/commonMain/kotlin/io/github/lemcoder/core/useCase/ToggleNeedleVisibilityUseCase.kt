package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository

interface ToggleNeedleVisibilityUseCase {
    suspend operator fun invoke(needleId: String): Result<Unit>

    companion object {
        fun create(): ToggleNeedleVisibilityUseCase {
            return ToggleNeedleVisibilityUseCaseImpl()
        }
    }
}

private class ToggleNeedleVisibilityUseCaseImpl(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) : ToggleNeedleVisibilityUseCase {
    override suspend fun invoke(needleId: String): Result<Unit> {
        return try {
            needleRepository.toggleNeedleVisibility(needleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
