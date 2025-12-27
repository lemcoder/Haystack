package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository

interface DeleteNeedleUseCase {
    suspend operator fun invoke(needleId: String): Result<Unit>

    companion object {
        fun create(): DeleteNeedleUseCase {
            return DeleteNeedleUseCaseImpl()
        }
    }
}

private class DeleteNeedleUseCaseImpl(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) : DeleteNeedleUseCase {
    override suspend fun invoke(needleId: String): Result<Unit> {
        return try {
            needleRepository.deleteNeedle(needleId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
