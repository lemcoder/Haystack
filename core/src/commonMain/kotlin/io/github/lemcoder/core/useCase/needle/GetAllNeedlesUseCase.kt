package io.github.lemcoder.core.useCase.needle

import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.Needle
import kotlinx.coroutines.flow.Flow

interface GetAllNeedlesUseCase {
    operator fun invoke(): Flow<List<Needle>>

    companion object {
        fun create(): GetAllNeedlesUseCase {
            return GetAllNeedlesUseCaseImpl()
        }
    }
}

private class GetAllNeedlesUseCaseImpl(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) : GetAllNeedlesUseCase {
    override fun invoke(): Flow<List<Needle>> {
        return needleRepository.needlesFlow
    }
}
