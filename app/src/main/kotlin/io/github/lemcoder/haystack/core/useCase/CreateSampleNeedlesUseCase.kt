package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.NeedleRepository
import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

class CreateSampleNeedlesUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    suspend operator fun invoke() {
        // Only create samples if no needles exist
        if (needleRepository.getAllNeedles().isNotEmpty()) {
            return
        }

        val samples = listOf(
            Needle(
                id = UUID.randomUUID().toString(),
                name = "Calculate Sum",
                description = "Calculates the sum of two numbers",
                pythonCode = """
result = a + b
print(f"The sum is: {result}")
                """.trimIndent(),
                args = listOf(
                    Needle.Arg(
                        name = "a",
                        type = NeedleType.Float,
                        description = "First number",
                        required = true
                    ),
                    Needle.Arg(
                        name = "b",
                        type = NeedleType.Float,
                        description = "Second number",
                        required = true
                    )
                ),
                returnType = NeedleType.Float,
                tags = listOf("math", "calculator"),
                isLLMGenerated = false
            ),
            Needle(
                id = UUID.randomUUID().toString(),
                name = "Text Analyzer",
                description = "Analyzes text and returns word count, character count, and sentence count",
                pythonCode = """
text = text.strip()
word_count = len(text.split())
char_count = len(text)
sentence_count = text.count('.') + text.count('!') + text.count('?')

print(f"Word count: {word_count}")
print(f"Character count: {char_count}")
print(f"Sentence count: {sentence_count}")
                """.trimIndent(),
                args = listOf(
                    Needle.Arg(
                        name = "text",
                        type = NeedleType.String,
                        description = "The text to analyze",
                        required = true
                    )
                ),
                returnType = NeedleType.String,
                tags = listOf("text", "analysis", "nlp"),
                isLLMGenerated = false
            ),
            Needle(
                id = UUID.randomUUID().toString(),
                name = "Temperature Converter",
                description = "Converts temperature between Celsius and Fahrenheit",
                pythonCode = """
if unit.lower() == 'c':
    result = (temperature * 9/5) + 32
    print(f"{temperature}°C = {result:.2f}°F")
elif unit.lower() == 'f':
    result = (temperature - 32) * 5/9
    print(f"{temperature}°F = {result:.2f}°C")
else:
    print("Invalid unit. Use 'C' for Celsius or 'F' for Fahrenheit")
                """.trimIndent(),
                args = listOf(
                    Needle.Arg(
                        name = "temperature",
                        type = NeedleType.Float,
                        description = "Temperature value to convert",
                        required = true
                    ),
                    Needle.Arg(
                        name = "unit",
                        type = NeedleType.String,
                        description = "Source unit: 'C' for Celsius or 'F' for Fahrenheit",
                        required = true
                    )
                ),
                returnType = NeedleType.String,
                tags = listOf("converter", "temperature", "utility"),
                isLLMGenerated = false
            ),
            Needle(
                id = UUID.randomUUID().toString(),
                name = "List Sorter",
                description = "Sorts a comma-separated list of numbers",
                pythonCode = """
# Parse the input string into a list of numbers
numbers = [float(x.strip()) for x in numbers_str.split(',')]

# Sort the numbers
if order.lower() == 'desc':
    sorted_numbers = sorted(numbers, reverse=True)
else:
    sorted_numbers = sorted(numbers)

# Print the result
print(f"Sorted list: {sorted_numbers}")
                """.trimIndent(),
                args = listOf(
                    Needle.Arg(
                        name = "numbers_str",
                        type = NeedleType.String,
                        description = "Comma-separated list of numbers",
                        required = true
                    ),
                    Needle.Arg(
                        name = "order",
                        type = NeedleType.String,
                        description = "Sort order: 'asc' or 'desc'",
                        required = false,
                        defaultValue = "\"asc\""
                    )
                ),
                returnType = NeedleType.String,
                tags = listOf("sorting", "utility", "list"),
                isLLMGenerated = false
            )
        )

        samples.forEach { needle ->
            needleRepository.saveNeedle(needle)
        }
    }
}
