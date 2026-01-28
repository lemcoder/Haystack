package io.github.lemcoder.core.needle

import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.utils.Log

/** Executor for needle tools that handles execution of needles with their arguments */
class NeedleToolExecutor(
    private val scriptExecutor: ScriptExecutor = createScriptExecutor()
) {

    /**
     * Executes a needle with pre-parsed parameters
     *
     * @param needle The needle to execute
     * @param params The parsed and validated parameters (map of argument names to values)
     * @return Result containing a type-safe NeedleResult with the actual typed value or error
     */
    fun executeNeedle(
        params: Map<String, Any>,
        needle: Needle,
    ): Result<NeedleResult> {
        return try {
            Log.d(TAG, "Executing needle: ${needle.name}")
            val code = with(LuaNeedleCodeBuilder()) {
                // Match params to their argument definitions in the needle
                params.forEach { (paramName, value) ->
                    val argDef = needle.args.find { it.name == paramName }
                        ?: throw IllegalArgumentException("Unknown parameter: $paramName")
                    addParam(paramName, argDef.type, value)
                }
                addCodeBlock(needle.code)
                build()
            }

            Log.d(TAG, "Generated code:\n$code")

            // Execute code with proper type handling based on return type
            val result = when (needle.returnType) {
                Needle.Arg.Type.String -> {
                    scriptExecutor.run<String>(code, params)
                        ?.let { NeedleResult.StringResult(it) }
                }
                Needle.Arg.Type.Int -> {
                    scriptExecutor.run<Int>(code, params)
                        ?.let { NeedleResult.IntResult(it) }
                }
                Needle.Arg.Type.Float -> {
                    scriptExecutor.run<Float>(code, params)
                        ?.let { NeedleResult.FloatResult(it) }
                }
                Needle.Arg.Type.Boolean -> {
                    scriptExecutor.run<Boolean>(code, params)
                        ?.let { NeedleResult.BooleanResult(it) }
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
