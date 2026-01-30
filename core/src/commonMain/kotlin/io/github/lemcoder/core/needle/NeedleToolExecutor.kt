package io.github.lemcoder.core.needle

import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleResult
import io.github.lemcoder.core.utils.Log

/** Executor for needle tools that handles execution of needles with their arguments */
class NeedleToolExecutor(private val scriptExecutor: ScriptExecutor = createScriptExecutor()) {

    /**
     * Executes a needle with pre-parsed parameters
     *
     * @param needle The needle to execute
     * @param params The parsed and validated parameters
     * @return Result containing a type-safe NeedleResult with the actual typed value or error
     */
    fun executeNeedle(needle: Needle, params: List<NeedleParameter>): Result<NeedleResult> {
        return try {
            Log.d(TAG, "Executing needle: ${needle.name}")
            val paramsMap = params.toParamMap()
            val code =
                with(LuaNeedleCodeBuilder()) {
                    // Use type-safe parameters
                    params.forEach { param ->
                        addParam(param.name, param.type, param.getValue())
                    }
                    addCodeBlock(needle.code)
                    build()
                }

            Log.d(TAG, "Generated code:\n$code")

            // Execute code with proper type handling based on return type
            val result =
                when (needle.returnType) {
                    Needle.Arg.Type.String -> {
                        scriptExecutor.run<String>(code, paramsMap)?.let {
                            NeedleResult.StringResult(it)
                        }
                    }
                    Needle.Arg.Type.Int -> {
                        scriptExecutor.run<Int>(code, paramsMap)?.let { NeedleResult.IntResult(it) }
                    }
                    Needle.Arg.Type.Float -> {
                        scriptExecutor.run<Float>(code, paramsMap)?.let {
                            NeedleResult.FloatResult(it)
                        }
                    }
                    Needle.Arg.Type.Boolean -> {
                        scriptExecutor.run<Boolean>(code, paramsMap)?.let {
                            NeedleResult.BooleanResult(it)
                        }
                    }
                }

            result?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("Script returned null"))
        } catch (e: Exception) {
            Log.e(TAG, "Error executing needle", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "NeedleToolExecutor"
    }
}
