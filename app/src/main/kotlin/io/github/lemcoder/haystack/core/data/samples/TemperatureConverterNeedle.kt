package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

object TemperatureConverterNeedle : SampleNeedle {
    override fun create() = Needle(
        id = UUID.randomUUID().toString(),
        name = "Temperature Converter",
        description = "Converts temperature between Celsius and Fahrenheit",
        pythonCode = """
if unit.lower() == 'c':
    result = (temperature * 9/5) + 32
    print(f"{temperature}C = {result:.2f}F")
elif unit.lower() == 'f':
    result = (temperature - 32) * 5/9
    print(f"{temperature}F = {result:.2f}C")
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
        returnType = NeedleType.String
    )
}
