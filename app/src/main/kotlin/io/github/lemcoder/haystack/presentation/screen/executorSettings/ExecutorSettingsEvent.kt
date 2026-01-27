package io.github.lemcoder.haystack.presentation.screen.executorSettings

import io.github.lemcoder.core.model.llm.ExecutorType
import io.github.lemcoder.core.model.llm.PromptExecutorConfig

sealed interface ExecutorSettingsEvent {
    data object NavigateBack : ExecutorSettingsEvent

    data object NavigateToAddExecutor : ExecutorSettingsEvent

    data class NavigateToEditExecutor(val executorType: ExecutorType) : ExecutorSettingsEvent

    data class SelectExecutor(val executorType: ExecutorType) : ExecutorSettingsEvent

    data class DeleteExecutor(val executorType: ExecutorType) : ExecutorSettingsEvent

    data class SaveExecutor(val config: PromptExecutorConfig) : ExecutorSettingsEvent

    data class UpdateExecutor(val config: PromptExecutorConfig) : ExecutorSettingsEvent
}
