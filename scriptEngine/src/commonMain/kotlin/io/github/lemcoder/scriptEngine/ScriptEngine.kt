package io.github.lemcoder.scriptEngine

// commonMain

interface ScriptEngine : AutoCloseable {
    fun eval(script: String): ScriptValue

    fun setGlobal(name: String, value: ScriptValue)

    fun registerFunction(name: String, fn: ScriptFunction)
}

expect fun instantiateScriptEngine(): ScriptEngine
