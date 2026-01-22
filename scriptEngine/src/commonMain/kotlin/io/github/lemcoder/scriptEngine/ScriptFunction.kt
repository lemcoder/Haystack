package io.github.lemcoder.scriptEngine

fun interface ScriptFunction {
    suspend operator fun invoke(args: List<ScriptValue>): ScriptValue
}