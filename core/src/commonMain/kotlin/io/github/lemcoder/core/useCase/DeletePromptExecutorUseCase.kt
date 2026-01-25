package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.PromptExecutorRepository
import io.github.lemcoder.core.model.llm.ExecutorType

interface DeletePromptExecutorUseCase {
    suspend operator fun invoke(executorType: ExecutorType): Result<Unit>

    companion object {
        fun create(): DeletePromptExecutorUseCase {
            return DeletePromptExecutorUseCaseImpl()
        }
    }
}

private class DeletePromptExecutorUseCaseImpl(
    private val promptExecutorRepository: PromptExecutorRepository = PromptExecutorRepository.Instance
) : DeletePromptExecutorUseCase {
    override suspend fun invoke(executorType: ExecutorType): Result<Unit> {
        return try {
            promptExecutorRepository.deleteExecutor(executorType)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
