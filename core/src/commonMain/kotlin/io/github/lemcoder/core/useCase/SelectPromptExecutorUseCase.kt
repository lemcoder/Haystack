package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.PromptExecutorRepository
import io.github.lemcoder.core.model.llm.ExecutorType

interface SelectPromptExecutorUseCase {
    suspend operator fun invoke(executorType: ExecutorType): Result<Unit>

    companion object {
        fun create(): SelectPromptExecutorUseCase {
            return SelectPromptExecutorUseCaseImpl()
        }
    }
}

private class SelectPromptExecutorUseCaseImpl(
    private val promptExecutorRepository: PromptExecutorRepository = PromptExecutorRepository.Instance
) : SelectPromptExecutorUseCase {
    override suspend fun invoke(executorType: ExecutorType): Result<Unit> {
        return try {
            promptExecutorRepository.selectExecutor(executorType)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
