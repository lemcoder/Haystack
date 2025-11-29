package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.repository.NeedleRepository
import io.github.lemcoder.haystack.core.model.needle.Needle
import kotlinx.coroutines.flow.Flow

class GetAllNeedlesUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    operator fun invoke(): Flow<List<Needle>> {
        return needleRepository.needlesFlow
    }
}
