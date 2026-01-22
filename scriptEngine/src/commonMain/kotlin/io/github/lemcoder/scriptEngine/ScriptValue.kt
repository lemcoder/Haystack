package io.github.lemcoder.scriptEngine

sealed class ScriptValue {
    data class Str(val value: String) : ScriptValue()
    data class Num(val value: Double) : ScriptValue()
    data class Bool(val value: Boolean) : ScriptValue()
    data class MapVal(val value: Map<String, ScriptValue>) : ScriptValue()
    data class ListVal(val value: List<ScriptValue>) : ScriptValue()
    object Nil : ScriptValue()
}

// commonMain

fun ScriptValue.asString(): String =
    (this as? ScriptValue.Str)?.value
        ?: error("Expected string, got $this")

fun ScriptValue.asBoolean(): Boolean =
    (this as? ScriptValue.Bool)?.value
        ?: error("Expected boolean, got $this")
