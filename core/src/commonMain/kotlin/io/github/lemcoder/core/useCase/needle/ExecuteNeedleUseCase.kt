package io.github.lemcoder.core.useCase.needle

import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.NeedleResult
import io.github.lemcoder.core.needle.NeedleArgumentParser
import io.github.lemcoder.core.needle.NeedleParameter
import io.github.lemcoder.core.needle.NeedleToolExecutor
import io.github.lemcoder.core.utils.Log

interface ExecuteNeedleUseCase {
    suspend operator fun invoke(needleId: String, args: Map<String, Any>): Result<String>

    companion object {
        fun create(): ExecuteNeedleUseCase {
            return ExecuteNeedleUseCaseImpl()
        }
    }
}

private class ExecuteNeedleUseCaseImpl(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance,
    private val needleArgumentParser: NeedleArgumentParser = NeedleArgumentParser(),
    private val needleToolExecutor: NeedleToolExecutor = NeedleToolExecutor()
) : ExecuteNeedleUseCase {
    override suspend fun invoke(needleId: String, args: Map<String, Any>): Result<String> {
        return try {
            val needle =
                needleRepository.getNeedleById(needleId)
                    ?: return Result.failure(
                        IllegalArgumentException("Needle not found: $needleId")
                    )

            // Parse and validate incoming args using NeedleArgumentParser
            val parsedArgs: List<NeedleParameter> = needleArgumentParser.parseFromMap(args, needle)

            val result = needleToolExecutor.executeNeedle(needle, parsedArgs)

            return result.mapCatching { needleResult ->
                when (needleResult) {
                    is NeedleResult.BooleanResult -> {
                        needleResult.value.toString()
                    }

                    is NeedleResult.FloatResult -> {
                        needleResult.value.toString()
                    }

                    is NeedleResult.IntResult -> {
                        needleResult.value.toString()
                    }

                    is NeedleResult.StringResult -> {
                        needleResult.value
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing needle", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "ExecuteNeedleUseCase"
    }
}
