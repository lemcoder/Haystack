package io.github.lemcoder.scriptEngine

import kotlinx.coroutines.runBlocking
import party.iroiro.luajava.Lua
import party.iroiro.luajava.LuaException
import party.iroiro.luajava.luaj.LuaJ
import party.iroiro.luajava.value.LuaValue

actual fun instantiateScriptEngine(): ScriptEngine {
    return AndroidScriptEngine()
}

class AndroidScriptEngine : ScriptEngine {
    private val lua: Lua = LuaJ()

    init {
        lua.openLibraries()
    }

    override fun eval(script: String): ScriptValue {
        return try {
            val results = lua.eval(script) // returns Array<LuaValue>
            if (results.isEmpty()) {
                ScriptValue.Nil
            } else {
                // Push the result onto the stack and convert it
                results[0].push(lua)
                val result = ScriptValueConverter.toScriptValue(lua, -1)
                lua.pop(1)
                result
            }
        } catch (e: LuaException) {
            throw RuntimeException("Lua execution error: ${e.message}", e)
        }
    }

    override fun setGlobal(name: String, value: ScriptValue) {
        ScriptValueConverter.pushToLua(lua, value)
        lua.setGlobal(name)
    }

    override fun registerFunction(name: String, fn: ScriptFunction) {
        lua.register(
            name,
            { luaInstance: Lua, luaArgs: Array<LuaValue> ->
                // Convert LuaValue[] to List<ScriptValue>
                val args = luaArgs.map { luaValue -> ScriptValueConverter.toScriptValue(luaValue) }

                // Execute the function
                val result = runBlocking { fn.invoke(args) }

                // Convert ScriptValue result to LuaValue and return as array
                arrayOf(ScriptValueConverter.toLuaValue(luaInstance, result))
            },
        )
    }

    override fun close() {
        lua.close()
    }
}
