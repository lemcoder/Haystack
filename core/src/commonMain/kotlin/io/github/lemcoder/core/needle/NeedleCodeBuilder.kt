package io.github.lemcoder.core.needle

import io.github.lemcoder.core.model.needle.Needle

interface NeedleCodeBuilder {
    fun addParam(name: String, type: Needle.Arg.Type, value: Any)
    fun addCodeBlock(code: String)
    fun build(): String
}

internal class LuaNeedleCodeBuilder() : NeedleCodeBuilder {
    private val codeLines = mutableListOf<String>()

    override fun addParam(name: String, type: Needle.Arg.Type, value: Any) {
        val luaValue = when (type) {
            Needle.Arg.Type.Boolean -> {
                (value as? Boolean)?.toString()
                    ?: error("Expected Boolean for $name, got ${value::class}")
            }

            Needle.Arg.Type.Float -> {
                (value as? Number)?.toDouble()?.toString()
                    ?: error("Expected Float/Double for $name, got ${value::class}")
            }

            Needle.Arg.Type.Int -> {
                (value as? Number)?.toInt()?.toString()
                    ?: error("Expected Int for $name, got ${value::class}")
            }

            Needle.Arg.Type.String -> {
                val s = value as? String
                    ?: error("Expected String for $name, got ${value::class}")
                "\"${escapeLuaString(s)}\""
            }
        }

        codeLines.add("local $name = $luaValue")
    }

    override fun addCodeBlock(code: String) {
        codeLines.add(code)
    }

    override fun build(): String {
        return codeLines.joinToString(separator = "\n")
    }

    private fun escapeLuaString(input: String): String {
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
