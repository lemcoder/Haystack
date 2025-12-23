package io.github.lemcoder.core.service.needle

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.extension.asAssistantMessage
import ai.koog.agents.core.dsl.extension.requestLLM
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import io.github.lemcoder.core.data.repository.NeedleRepository
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleType
import io.github.lemcoder.core.utils.Log
import io.github.lemcoder.core.utils.currentTimeMillis
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Service that generates needles using LLM via Koog and OpenRouter. */
class NeedleGeneratorService(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    // This API key has been rolled you thief - use your own OpenRouter key!
    private val openRouterExecutor =
        simpleOpenRouterExecutor(
            "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"
        )

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Generates a needle based on user-provided functionality description.
     *
     * @param functionalityDescription The description of what the needle should do
     * @return Result containing the generated Needle or an error
     */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun generateNeedle(functionalityDescription: String): Result<Needle> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Generating needle for: $functionalityDescription")

                // Create the agent with a simple strategy that just gets the response
                val strategy =
                    functionalStrategy<String, String>("needle-generator") { input ->
                        val response = requestLLM(input)
                        response.asAssistantMessage().content
                    }

                val agentConfig =
                    AIAgentConfig(
                        prompt = createGenerationPrompt(functionalityDescription),
                        model = OpenRouterModels.Claude3Opus,
                        maxAgentIterations = 1,
                    )

                val agent =
                    AIAgent(
                        promptExecutor = openRouterExecutor,
                        strategy = strategy,
                        agentConfig = agentConfig,
                    )

                // Run the agent with the functionality description
                val response = agent.run(functionalityDescription)

                Log.d(TAG, "LLM Response: $response")

                // Parse the JSON response
                val needleSpec = parseNeedleSpec(response)

                // Create the Needle object
                val needle =
                    Needle(
                        id = Uuid.random().toString(),
                        name = needleSpec.name,
                        description = needleSpec.description,
                        pythonCode = needleSpec.pythonCode,
                        args =
                            needleSpec.args.map { arg ->
                                Needle.Arg(
                                    name = arg.name,
                                    type = parseNeedleType(arg.type),
                                    description = arg.description,
                                    required = arg.required,
                                    defaultValue = arg.defaultValue,
                                )
                            },
                        returnType = parseNeedleType(needleSpec.returnType),
                        createdAt = Clock.System.currentTimeMillis(),
                        updatedAt = Clock.System.currentTimeMillis(),
                    )

                // Save the needle to repository
                needleRepository.saveNeedle(needle)

                Log.d(TAG, "Needle generated and saved: ${needle.name}")
                Result.success(needle)
            } catch (e: Exception) {
                Log.e(TAG, "Error generating needle", e)
                Result.failure(e)
            }
        }
    }

    private fun createGenerationPrompt(functionalityDescription: String) =
        prompt("needle-generator") {
            system(
                """
                You are an expert Python developer and tool designer. Your task is to generate a complete Needle specification based on user requirements.

                A Needle is a Python function that can be used as a tool by an AI agent. Each Needle has:
                - A name (short, descriptive)
                - A description (what it does)
                - Arguments with types and descriptions
                - A return type
                - Python code that implements the functionality

                Available types: String, Int, Float, Boolean, Image, Any

                You MUST respond with ONLY a valid JSON object with this exact structure (no markdown, no code blocks, no explanations):
                {
                  "name": "Needle Name",
                  "description": "What this needle does",
                  "args": [
                    {
                      "name": "arg_name",
                      "type": "String",
                      "description": "What this argument is for",
                      "required": true,
                      "defaultValue": null
                    }
                  ],
                  "returnType": "String",
                  "pythonCode": "# Python code here\nresult = 'example'\nprint(result)"
                }

                Important rules for Python code:
                1. The code should use the argument names as variables (they will be pre-defined)
                2. Use print() to output the result - the last printed value is the return value
                3. Keep it simple and focused on the task
                4. Include basic error handling if needed
                5. Don't import unavailable libraries (assume basic Python 3 standard library)
                6. Use proper string escaping in JSON

                Generate ONLY the JSON object, nothing else.
                """
                    .trimIndent()
            )
            user(
                """
            Generate a Needle for the following functionality:
            
            $functionalityDescription
            
            Remember: Respond with ONLY the JSON object, no markdown formatting, no explanations.
            """
                    .trimIndent()
            )
        }

    private fun parseNeedleSpec(response: String): NeedleSpec {
        // Clean up the response - remove markdown code blocks if present
        val cleanedResponse =
            response.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        return json.decodeFromString(NeedleSpec.serializer(), cleanedResponse)
    }

    private fun parseNeedleType(typeString: String): NeedleType {
        return when (typeString.lowercase()) {
            "string" -> NeedleType.String
            "int",
            "integer" -> NeedleType.Int

            "float",
            "double" -> NeedleType.Float

            "boolean",
            "bool" -> NeedleType.Boolean

            "image" -> NeedleType.Image
            "any" -> NeedleType.Any
            else -> NeedleType.String // Default to String
        }
    }

    @Serializable
    private data class NeedleSpec(
        val name: String,
        val description: String,
        val args: List<ArgSpec>,
        val returnType: String,
        val pythonCode: String,
    )

    @Serializable
    private data class ArgSpec(
        val name: String,
        val type: String,
        val description: String,
        val required: Boolean = true,
        val defaultValue: String? = null,
    )

    companion object {
        private const val TAG = "NeedleGeneratorService"

        val Instance: NeedleGeneratorService by lazy { NeedleGeneratorService() }
    }
}
