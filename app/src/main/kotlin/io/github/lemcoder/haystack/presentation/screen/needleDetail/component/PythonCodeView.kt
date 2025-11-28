package io.github.lemcoder.haystack.presentation.screen.needleDetail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PythonCodeView(
    code: String,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()

    Text(
        text = highlightPythonSyntax(code),
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState),
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
}

@Composable
private fun highlightPythonSyntax(code: String) = buildAnnotatedString {
    val keywords = setOf(
        "if", "elif", "else", "for", "while", "def", "class", "import", "from",
        "return", "yield", "break", "continue", "pass", "try", "except", "finally",
        "with", "as", "raise", "assert", "del", "global", "nonlocal", "lambda",
        "True", "False", "None", "and", "or", "not", "in", "is"
    )

    val builtins = setOf(
        "print", "len", "range", "str", "int", "float", "list", "dict", "set",
        "tuple", "bool", "type", "isinstance", "enumerate", "zip", "map", "filter",
        "sum", "max", "min", "sorted", "open", "abs", "round"
    )

    val keywordColor = Color(0xFF569CD6)      // Blue
    val stringColor = Color(0xFFCE9178)       // Orange
    val commentColor = Color(0xFF6A9955)      // Green
    val numberColor = Color(0xFFB5CEA8)       // Light green
    val builtinColor = Color(0xFFDCDCAA)      // Yellow
    val defaultColor = MaterialTheme.colorScheme.onSurfaceVariant

    val lines = code.lines()
    lines.forEachIndexed { index, line ->
        var i = 0
        while (i < line.length) {
            when {
                // Comments
                line[i] == '#' -> {
                    withStyle(SpanStyle(color = commentColor, fontWeight = FontWeight.Normal)) {
                        append(line.substring(i))
                    }
                    i = line.length
                }

                // Strings (triple quotes)
                line.startsWith("\"\"\"", i) || line.startsWith("'''", i) -> {
                    val quote = line.substring(i, i + 3)
                    val end = line.indexOf(quote, i + 3)
                    if (end != -1) {
                        withStyle(SpanStyle(color = stringColor)) {
                            append(line.substring(i, end + 3))
                        }
                        i = end + 3
                    } else {
                        withStyle(SpanStyle(color = stringColor)) {
                            append(line.substring(i))
                        }
                        i = line.length
                    }
                }

                // Strings (single or double quotes)
                line[i] == '"' || line[i] == '\'' -> {
                    val quote = line[i]
                    var j = i + 1
                    var escaped = false
                    while (j < line.length) {
                        if (line[j] == '\\' && !escaped) {
                            escaped = true
                        } else if (line[j] == quote && !escaped) {
                            break
                        } else {
                            escaped = false
                        }
                        j++
                    }
                    withStyle(SpanStyle(color = stringColor)) {
                        append(line.substring(i, minOf(j + 1, line.length)))
                    }
                    i = minOf(j + 1, line.length)
                }

                // Numbers
                line[i].isDigit() -> {
                    var j = i
                    while (j < line.length && (line[j].isDigit() || line[j] == '.')) {
                        j++
                    }
                    withStyle(SpanStyle(color = numberColor)) {
                        append(line.substring(i, j))
                    }
                    i = j
                }

                // Keywords and identifiers
                line[i].isLetter() || line[i] == '_' -> {
                    var j = i
                    while (j < line.length && (line[j].isLetterOrDigit() || line[j] == '_')) {
                        j++
                    }
                    val word = line.substring(i, j)
                    val style = when {
                        word in keywords -> SpanStyle(
                            color = keywordColor,
                            fontWeight = FontWeight.Bold
                        )

                        word in builtins -> SpanStyle(color = builtinColor)
                        else -> SpanStyle(color = defaultColor)
                    }
                    withStyle(style) {
                        append(word)
                    }
                    i = j
                }

                // Default characters
                else -> {
                    withStyle(SpanStyle(color = defaultColor)) {
                        append(line[i])
                    }
                    i++
                }
            }
        }

        // Add newline except for last line
        if (index < lines.size - 1) {
            append("\n")
        }
    }
}
