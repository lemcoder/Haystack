package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.PromptExecutorRepository
import io.github.lemcoder.core.model.llm.PromptExecutorConfig
import kotlinx.coroutines.flow.Flow

interface GetAllPromptExecutorsUseCase {
    operator fun invoke(): Flow<List<PromptExecutorConfig>>

    companion object {
        fun create(): GetAllPromptExecutorsUseCase {
            return GetAllPromptExecutorsUseCaseImpl()
        }
    }
}

private class GetAllPromptExecutorsUseCaseImpl(
    private val promptExecutorRepository: PromptExecutorRepository = PromptExecutorRepository.Instance
) : GetAllPromptExecutorsUseCase {
    override fun invoke(): Flow<List<PromptExecutorConfig>> {
        return promptExecutorRepository.executorConfigsFlow
    }
}
