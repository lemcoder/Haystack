package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository

class IsNeedleHiddenUseCase(
  private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
  suspend operator fun invoke(needleId: String): Boolean {
    return needleRepository.isNeedleHidden(needleId)
  }
}
