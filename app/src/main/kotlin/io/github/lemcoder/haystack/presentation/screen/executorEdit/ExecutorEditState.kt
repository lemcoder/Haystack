package io.github.lemcoder.haystack.presentation.screen.executorEdit

import io.github.lemcoder.core.model.llm.ExecutorType

data class ExecutorEditState(
    val executorType: ExecutorType? = null,
    val selectedModelName: String = "",
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null,
)
