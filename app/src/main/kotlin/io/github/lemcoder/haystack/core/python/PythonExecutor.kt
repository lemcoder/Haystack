package io.github.lemcoder.haystack.core.python

import android.util.Log
import com.chaquo.python.PyException
import com.chaquo.python.PyObject
import com.chaquo.python.Python

/**
 * Helper class for executing Python code dynamically using the Chaquopy interpreter.
 *
 * This class uses the interpreter.py module to execute arbitrary Python code in a separate thread
 * with timeout handling.
 */
object PythonExecutor {
  private const val TAG = "PythonExecutor"

  /**
   * Executes Python code and returns the output as a string.
   *
   * @param code The Python code to execute
   * @param captureOutput Whether to capture and return the stdout output (default: true)
   * @return The output from Python's stdout, or an error message if execution fails
   * @throws PyException if there's a Python-related error
   */
  fun execute(code: String, captureOutput: Boolean = true): String {
    val py = Python.getInstance()

    // Get system modules
    val sys = py.getModule("sys")
    val io = py.getModule("io")
    val interpreter = py.getModule("interpreter")

    var output = ""
    var previousStdout: PyObject? = null

    try {
      if (captureOutput) {
        // Save previous stdout
        previousStdout = sys["stdout"]

        // Redirect stdout to capture output
        val textOutputStream = io.callAttr("StringIO")
        sys.put("stdout", textOutputStream)

        // Execute the Python code
        interpreter.callAttr("mainTextCode", code)

        // Get the output
        output = textOutputStream.callAttr("getvalue").toString()
      } else {
        // Execute without capturing output
        interpreter.callAttr("mainTextCode", code)
      }

      return output
    } catch (e: PyException) {
      Log.e(TAG, "Python execution error: ${e.message}", e)
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Error executing Python code", e)
      throw e
    } finally {
      // Restore previous stdout if it was redirected
      if (captureOutput && previousStdout != null) {
        sys.put("stdout", previousStdout)
      }
    }
  }

  /**
   * Executes Python code with error handling and returns a Result object.
   *
   * @param code The Python code to execute
   * @param captureOutput Whether to capture and return the stdout output (default: true)
   * @return Result containing the output or error information
   */
  fun executeSafe(code: String, captureOutput: Boolean = true): Result<String> {
    return try {
      Result.success(execute(code, captureOutput))
    } catch (e: PyException) {
      Log.e(TAG, "Python execution failed: ${e.message}", e)
      Result.failure(e)
    } catch (e: Exception) {
      Log.e(TAG, "Execution failed", e)
      Result.failure(e)
    }
  }

  /**
   * Executes Python code and parses the output with a custom parser.
   *
   * @param code The Python code to execute
   * @param parser Function to parse the output string into the desired type
   * @return Result containing the parsed value or error information
   */
  fun <T> executeAndParse(code: String, parser: (String) -> T): Result<T> {
    return executeSafe(code).mapCatching { output -> parser(output.trim()) }
  }
}
