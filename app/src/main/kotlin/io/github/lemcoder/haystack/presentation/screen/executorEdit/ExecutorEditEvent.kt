package io.github.lemcoder.haystack.presentation.screen.executorEdit

import io.github.lemcoder.core.model.llm.ExecutorType

sealed interface ExecutorEditEvent {
    data object NavigateBack : ExecutorEditEvent

    data class UpdateExecutorType(val executorType: ExecutorType) : ExecutorEditEvent

    data class UpdateModelName(val modelName: String) : ExecutorEditEvent

    data class UpdateApiKey(val apiKey: String) : ExecutorEditEvent

    data class UpdateBaseUrl(val baseUrl: String) : ExecutorEditEvent

    data object SaveExecutor : ExecutorEditEvent
}
