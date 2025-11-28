package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

object ListSorterNeedle : SampleNeedle {
    override fun create() = Needle(
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
        returnType = NeedleType.String
    )
}
