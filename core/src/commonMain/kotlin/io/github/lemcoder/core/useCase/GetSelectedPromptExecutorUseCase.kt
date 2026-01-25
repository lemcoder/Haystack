package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.PromptExecutorRepository
import io.github.lemcoder.core.model.llm.PromptExecutorConfig
import kotlinx.coroutines.flow.Flow

interface GetSelectedPromptExecutorUseCase {
    operator fun invoke(): Flow<PromptExecutorConfig?>

    companion object {
        fun create(): GetSelectedPromptExecutorUseCase {
            return GetSelectedPromptExecutorUseCaseImpl()
        }
    }
}

private class GetSelectedPromptExecutorUseCaseImpl(
    private val promptExecutorRepository: PromptExecutorRepository = PromptExecutorRepository.Instance
) : GetSelectedPromptExecutorUseCase {
    override fun invoke(): Flow<PromptExecutorConfig?> {
        return promptExecutorRepository.selectedExecutorFlow
    }
}
