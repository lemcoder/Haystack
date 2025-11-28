package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

object CalculateSumNeedle : SampleNeedle {
    override fun create() = Needle(
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
        returnType = NeedleType.Float
    )
}
