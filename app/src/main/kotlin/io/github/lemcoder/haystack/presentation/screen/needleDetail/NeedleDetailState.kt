package io.github.lemcoder.haystack.presentation.screen.needleDetail

import io.github.lemcoder.core.model.needle.Needle

data class NeedleDetailState(
    val needle: Needle? = null,
    val isLoading: Boolean = false,
    val isExecuting: Boolean = false,
    val executionResult: ExecutionResult? = null,
    val showArgumentsDialog: Boolean = false,
    val argumentValues: Map<String, String> = emptyMap(),
    val errorMessage: String? = null,
)

sealed class ExecutionResult {
    data class TextResult(val output: String) : ExecutionResult()

    data class ImageResult(val imagePath: String) : ExecutionResult()

    data class ErrorResult(val error: String) : ExecutionResult()
}
