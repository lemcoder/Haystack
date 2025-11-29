package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.NeedleRepository

class ObserveNeedlesUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    operator fun invoke() = needleRepository.needlesFlow
}