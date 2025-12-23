package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository

class ToggleNeedleVisibilityUseCase(
  private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
  suspend operator fun invoke(needleId: String): Result<Unit> {
    return try {
      needleRepository.toggleNeedleVisibility(needleId)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
