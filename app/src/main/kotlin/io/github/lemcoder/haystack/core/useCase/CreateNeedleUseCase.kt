package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.repository.NeedleRepository
import io.github.lemcoder.haystack.core.model.needle.Needle
import java.util.UUID

class CreateNeedleUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    suspend operator fun invoke(needle: Needle): Result<Needle> {
        return try {
            // Ensure the needle has a valid ID
            val needleToSave = if (needle.id.isBlank()) {
                needle.copy(id = UUID.randomUUID().toString())
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
