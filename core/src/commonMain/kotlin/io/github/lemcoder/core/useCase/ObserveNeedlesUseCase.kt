package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository

class ObserveNeedlesUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    operator fun invoke() = needleRepository.visibleNeedlesFlow
}
