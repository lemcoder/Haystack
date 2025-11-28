package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

object TextAnalyzerNeedle : SampleNeedle {
    override fun create() = Needle(
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
        returnType = NeedleType.String
    )
}
