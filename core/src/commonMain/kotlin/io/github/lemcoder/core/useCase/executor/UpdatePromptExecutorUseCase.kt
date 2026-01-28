package io.github.lemcoder.core.useCase.executor

import io.github.lemcoder.core.data.repository.PromptExecutorRepository
import io.github.lemcoder.core.model.llm.PromptExecutorConfig

interface UpdatePromptExecutorUseCase {
    suspend operator fun invoke(config: PromptExecutorConfig): Result<PromptExecutorConfig>

    companion object {
        fun create(): UpdatePromptExecutorUseCase {
            return UpdatePromptExecutorUseCaseImpl()
        }
    }
}

private class UpdatePromptExecutorUseCaseImpl(
    private val promptExecutorRepository: PromptExecutorRepository =
        PromptExecutorRepository.Instance
) : UpdatePromptExecutorUseCase {
    override suspend fun invoke(config: PromptExecutorConfig): Result<PromptExecutorConfig> {
        return try {
            promptExecutorRepository.updateExecutor(config)
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
