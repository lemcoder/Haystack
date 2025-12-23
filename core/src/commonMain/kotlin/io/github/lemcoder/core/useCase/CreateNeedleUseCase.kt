package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.Needle
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class CreateNeedleUseCase(
  private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
  @OptIn(ExperimentalUuidApi::class)
  suspend operator fun invoke(needle: Needle): Result<Needle> {
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
