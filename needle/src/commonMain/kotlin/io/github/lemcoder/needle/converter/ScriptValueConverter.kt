package io.github.lemcoder.needle.converter

import io.github.lemcoder.scriptEngine.ScriptValue

object ScriptValueConverter {
    fun toScriptValue(value: Any?): ScriptValue =
        when (value) {
            null -> ScriptValue.Nil
            is Boolean -> ScriptValue.Bool(value)
            is Number -> ScriptValue.Num(value.toDouble())
            is String -> ScriptValue.Str(value)
            is List<*> -> ScriptValue.ListVal(value.map { toScriptValue(it) })
            is Map<*, *> ->
                ScriptValue.MapVal(
                    value.entries
                        .filter { it.key is String }
                        .associate { (k, v) -> k as String to toScriptValue(v) }
                )

            else -> ScriptValue.Str(value.toString())
        }

    fun toKotlin(scriptValue: ScriptValue): Any? =
        when (scriptValue) {
            ScriptValue.Nil -> null
            is ScriptValue.Bool -> scriptValue.value
            is ScriptValue.Num -> scriptValue.value
            is ScriptValue.Str -> scriptValue.value
            is ScriptValue.ListVal -> scriptValue.value.map { toKotlin(it) }
            is ScriptValue.MapVal -> scriptValue.value.mapValues { toKotlin(it.value) }
        }
}
