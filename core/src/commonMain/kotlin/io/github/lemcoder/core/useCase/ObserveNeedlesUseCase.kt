package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.Needle
import kotlinx.coroutines.flow.Flow

interface ObserveNeedlesUseCase {
    operator fun invoke(): Flow<List<Needle>>

    companion object {
        fun create(): ObserveNeedlesUseCase {
            return ObserveNeedlesUseCaseImpl()
        }
    }
}

private class ObserveNeedlesUseCaseImpl(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) : ObserveNeedlesUseCase {
    override fun invoke(): Flow<List<Needle>> = needleRepository.visibleNeedlesFlow
}
