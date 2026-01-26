package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.Needle

interface CreateSampleNeedlesUseCase {
    suspend operator fun invoke()

    companion object {
        fun create(): CreateSampleNeedlesUseCase {
            return CreateSampleNeedlesUseCaseImpl()
        }
    }
}

private class CreateSampleNeedlesUseCaseImpl(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) : CreateSampleNeedlesUseCase {
    private val sampleNeedles: List<Needle> = emptyList()

    override suspend fun invoke() {
        // Clear all existing needles and recreate samples
        // This ensures we always have the latest sample needles
        needleRepository.deleteAllNeedles()

        sampleNeedles.forEach { sampleNeedle -> needleRepository.saveNeedle(sampleNeedle) }
    }
}
