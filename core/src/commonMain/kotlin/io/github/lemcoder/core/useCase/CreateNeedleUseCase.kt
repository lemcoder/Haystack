package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.Needle
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface CreateNeedleUseCase {
    suspend operator fun invoke(needle: Needle): Result<Needle>

    companion object {
        fun create(): CreateNeedleUseCase {
            return CreateNeedleUseCaseImpl()
        }
    }
}

private class CreateNeedleUseCaseImpl(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) : CreateNeedleUseCase {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun invoke(needle: Needle): Result<Needle> {
        return try {
            // Ensure the needle has a valid ID
            val needleToSave =
                if (needle.id.isBlank()) {
                    needle.copy(id = Uuid.random().toString())
                } else {
                    needle
                }

            needleRepository.saveNeedle(needleToSave)
            Result.success(needleToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
