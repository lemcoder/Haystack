package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.data.repository.PromptExecutorRepository
import io.github.lemcoder.core.model.llm.PromptExecutorConfig

interface SavePromptExecutorUseCase {
    suspend operator fun invoke(config: PromptExecutorConfig): Result<PromptExecutorConfig>

    companion object {
        fun create(): SavePromptExecutorUseCase {
            return SavePromptExecutorUseCaseImpl()
        }
    }
}

private class SavePromptExecutorUseCaseImpl(
    private val promptExecutorRepository: PromptExecutorRepository = PromptExecutorRepository.Instance
) : SavePromptExecutorUseCase {
    override suspend fun invoke(config: PromptExecutorConfig): Result<PromptExecutorConfig> {
        return try {
            promptExecutorRepository.saveExecutor(config)
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
