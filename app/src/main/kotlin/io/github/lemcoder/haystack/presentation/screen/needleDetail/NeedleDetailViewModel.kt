package io.github.lemcoder.haystack.presentation.screen.needleDetail

import android.util.Log
import androidx.lifecycle.viewModelScope
import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.useCase.needle.ExecuteNeedleUseCase
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NeedleDetailViewModel(
    private val needleId: String,
    private val needleRepository: NeedleRepository = NeedleRepository.Instance,
    private val executeNeedleUseCase: ExecuteNeedleUseCase = ExecuteNeedleUseCase.create(),
    private val navigationService: NavigationService = NavigationService.Instance,
) : MviViewModel<NeedleDetailState, NeedleDetailEvent>() {

    private val _state = MutableStateFlow(NeedleDetailState())
    override val state: StateFlow<NeedleDetailState> = _state.asStateFlow()

    init {
        loadNeedle()
    }

    override fun onEvent(event: NeedleDetailEvent) {
        when (event) {
            NeedleDetailEvent.NavigateBack -> navigationService.navigateBack()
            NeedleDetailEvent.ExecuteNeedle -> checkArgumentsAndExecute()
            NeedleDetailEvent.DismissResult -> dismissResult()
            NeedleDetailEvent.ShowArgumentsDialog -> showArgumentsDialog()
            NeedleDetailEvent.DismissArgumentsDialog -> dismissArgumentsDialog()
            is NeedleDetailEvent.UpdateArgument -> updateArgument(event.argName, event.value)
            NeedleDetailEvent.ConfirmAndExecute -> executeWithArguments()
        }
    }

    private fun loadNeedle() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val needle = needleRepository.getNeedleById(needleId)
                if (needle == null) {
                    SnackbarUtil.showSnackbar("Needle not found")
                    navigationService.navigateBack()
                    return@launch
                }

                // Initialize argument values with defaults
                val initialArgValues =
                    needle.args.associate { arg -> arg.name to (arg.defaultValue ?: "") }

                _state.value =
                    _state.value.copy(
                        needle = needle,
                        argumentValues = initialArgValues,
                        isLoading = false,
                    )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading needle", e)
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = "Error loading needle: ${e.message}",
                    )
                SnackbarUtil.showSnackbar("Error loading needle")
            }
        }
    }

    private fun checkArgumentsAndExecute() {
        val needle = _state.value.needle ?: return

        if (needle.args.isEmpty()) {
            // No arguments, execute directly
            executeWithArguments()
        } else {
            // Show arguments dialog
            showArgumentsDialog()
        }
    }

    private fun showArgumentsDialog() {
        _state.value = _state.value.copy(showArgumentsDialog = true)
    }

    private fun dismissArgumentsDialog() {
        _state.value = _state.value.copy(showArgumentsDialog = false)
    }

    private fun updateArgument(argName: String, value: String) {
        val currentValues = _state.value.argumentValues.toMutableMap()
        currentValues[argName] = value
        _state.value = _state.value.copy(argumentValues = currentValues)
    }

    private fun executeWithArguments() {
        val needle = _state.value.needle ?: return

        viewModelScope.launch {
            try {
                _state.value =
                    _state.value.copy(
                        isExecuting = true,
                        showArgumentsDialog = false,
                        executionResult = null,
                    )

                // Convert string values to appropriate types
                val args = mutableMapOf<String, Any>()
                needle.args.forEach { arg ->
                    val stringValue = _state.value.argumentValues[arg.name] ?: ""

                    // Skip empty optional arguments
                    if (!arg.required && stringValue.isBlank()) {
                        return@forEach
                    }

                    // Validate required arguments
                    if (arg.required && stringValue.isBlank()) {
                        throw IllegalArgumentException("Required argument '${arg.name}' is missing")
                    }

                    // Convert to appropriate type
                    val value = convertStringToType(stringValue, arg.type)
                    args[arg.name] = value
                }

                // Execute the needle
                val result = withContext(Dispatchers.IO) { executeNeedleUseCase(needle.id, args) }

                // Process the result
                result.fold(
                    onSuccess = { output -> handleExecutionSuccess(output) },
                    onFailure = { error -> handleExecutionError(error) },
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error executing needle", e)
                _state.value =
                    _state.value.copy(
                        isExecuting = false,
                        executionResult = ExecutionResult.ErrorResult(e.message ?: "Unknown error"),
                    )
            }
        }
    }

    private fun convertStringToType(value: String, type: Needle.Arg.Type): Any {
        return when (type) {
            Needle.Arg.Type.String -> value
            Needle.Arg.Type.Int ->
                value.toIntOrNull() ?: throw IllegalArgumentException("Invalid integer: $value")

            Needle.Arg.Type.Float ->
                value.toFloatOrNull() ?: throw IllegalArgumentException("Invalid number: $value")

            Needle.Arg.Type.Boolean -> value.lowercase() in listOf("true", "1", "yes")
        }
    }

    private fun handleExecutionSuccess(output: String) {
        // Check if output contains an image path
        val imagePath = extractImagePath(output)

        val result =
            if (imagePath != null && File(imagePath).exists()) {
                ExecutionResult.ImageResult(imagePath)
            } else {
                ExecutionResult.TextResult(output)
            }

        _state.value = _state.value.copy(isExecuting = false, executionResult = result)
    }

    private fun handleExecutionError(error: Throwable) {
        Log.e(TAG, "Needle execution failed", error)
        _state.value =
            _state.value.copy(
                isExecuting = false,
                executionResult = ExecutionResult.ErrorResult(error.message ?: "Execution failed"),
            )
    }

    private fun extractImagePath(output: String): String? {
        // Look for common image file extensions in the output
        val imageExtensions = listOf(".png", ".jpg", ".jpeg", ".bmp", ".gif")
        val lines = output.lines()

        for (line in lines) {
            val trimmed = line.trim()
            if (imageExtensions.any { trimmed.endsWith(it, ignoreCase = true) }) {
                if (File(trimmed).exists()) {
                    return trimmed
                }
            }
        }

        return null
    }

    private fun dismissResult() {
        _state.value = _state.value.copy(executionResult = null)
    }

    companion object {
        private const val TAG = "NeedleDetailViewModel"
    }
}
