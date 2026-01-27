package io.github.lemcoder.haystack.presentation.screen.executorSettings

import io.github.lemcoder.core.model.llm.PromptExecutorConfig

data class ExecutorSettingsState(
    val executors: List<PromptExecutorConfig> = emptyList(),
    val selectedExecutor: PromptExecutorConfig? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
