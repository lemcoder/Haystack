package io.github.lemcoder.haystack.core.useCase

import android.util.Log
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.haystack.core.data.repository.NeedleRepository
import io.github.lemcoder.haystack.core.python.PythonExecutor
import io.github.lemcoder.haystack.core.python.PythonValueFormatter
import io.github.lemcoder.haystack.core.service.needle.NeedleToolExecutor

class ExecuteNeedleUseCase(
  private val needleRepository: NeedleRepository = NeedleRepository.Instance,
  private val needleToolExecutor: NeedleToolExecutor = NeedleToolExecutor(),
) {
  suspend operator fun invoke(needleId: String, args: Map<String, Any>): Result<String> {
    return try {
      val needle =
        needleRepository.getNeedleById(needleId)
          ?: return Result.failure(IllegalArgumentException("Needle not found: $needleId"))

      // Validate arguments
      validateArguments(needle, args)

      // Build the Python code with arguments
      val pythonCode = buildPythonCode(needle, args)

      Log.d(TAG, "Executing needle: ${needle.name}")
      Log.d(TAG, "Python code:\n$pythonCode")

      // Execute the Python code
      val result = PythonExecutor.executeSafe(pythonCode)

      result
    } catch (e: Exception) {
      Log.e(TAG, "Error executing needle", e)
      Result.failure(e)
    }
  }

  private fun validateArguments(needle: Needle, args: Map<String, Any>) {
    // Check required arguments
    needle.args
      .filter { it.required }
      .forEach { arg ->
        if (!args.containsKey(arg.name)) {
          throw IllegalArgumentException("Missing required argument: ${arg.name}")
        }
      }

    // Type checking could be added here
    args.forEach { (name, value) ->
      val argDef = needle.args.find { it.name == name }
      if (argDef == null) {
        Log.w(TAG, "Unknown argument provided: $name")
      }
    }
  }

  private fun buildPythonCode(needle: Needle, args: Map<String, Any>): String {
    // Build variable assignments for each argument
    val argsCode =
      args.entries.joinToString("\n") { (name, value) ->
        val argDef = needle.args.find { it.name == name }
        val formattedValue = PythonValueFormatter.format(value, argDef?.type)
        "$name = $formattedValue"
      }

    // Add default values for optional arguments not provided
    val defaultsCode =
      needle.args
        .filter { !it.required && it.defaultValue != null && !args.containsKey(it.name) }
        .joinToString("\n") { arg -> "${arg.name} = ${arg.defaultValue}" }

    val parts = mutableListOf<String>()

    if (argsCode.isNotBlank()) {
      parts.add("# Arguments\n$argsCode")
    }

    if (defaultsCode.isNotBlank()) {
      parts.add("# Defaults\n$defaultsCode")
    }

    parts.add("# Needle code\n${needle.pythonCode}")

    return parts.joinToString("\n\n")
  }

  companion object {
    private const val TAG = "ExecuteNeedleUseCase"
  }
}
