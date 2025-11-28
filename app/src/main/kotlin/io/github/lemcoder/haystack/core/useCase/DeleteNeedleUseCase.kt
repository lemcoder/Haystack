package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.NeedleRepository

class DeleteNeedleUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    suspend operator fun invoke(needleId: String): Result<Unit> {
        return try {
            needleRepository.deleteNeedle(needleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
