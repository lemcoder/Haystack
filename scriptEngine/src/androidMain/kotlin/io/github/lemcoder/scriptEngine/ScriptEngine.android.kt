package io.github.lemcoder.scriptEngine

import kotlinx.coroutines.runBlocking
import party.iroiro.luajava.Lua
import party.iroiro.luajava.LuaException
import party.iroiro.luajava.lua55.Lua55
import party.iroiro.luajava.value.LuaValue

actual fun instantiateScriptEngine(): ScriptEngine {
    return AndroidScriptEngine()
}

class AndroidScriptEngine : ScriptEngine {
    private val lua: Lua = Lua55()

    init {
        lua.openLibraries()
    }

    override fun eval(script: String): ScriptValue {
        return try {
            val results = lua.eval(script) // returns Array<LuaValue>
            if (results.isEmpty()) {
                ScriptValue.Nil
            } else {
                ScriptValueConverter.toScriptValue(results[0])
            }
        } catch (e: LuaException) {
            throw RuntimeException("Lua execution error: ${e.message}", e)
        }
    }



    override fun setGlobal(name: String, value: ScriptValue) {
        pushScriptValue(value)
        lua.setGlobal(name)
    }

    override fun registerFunction(name: String, fn: ScriptFunction) {
        lua.register(name, { luaInstance: Lua, luaArgs: Array<LuaValue> ->
            // Convert LuaValue[] to List<ScriptValue>
            val args = luaArgs.map { luaValue ->
                luaValueToScriptValue(luaValue)
            }
            
            // Execute the function
            val result = runBlocking {
                fn.invoke(args)
            }
            
            // Convert ScriptValue result to LuaValue and return as array
            arrayOf(scriptValueToLuaValue(luaInstance, result))
        })
    }

    override fun close() {
        lua.close()
    }

    private fun pushScriptValue(value: ScriptValue) {
        pushScriptValueToLua(lua, value)
    }

    private fun pushScriptValueToLua(luaInstance: Lua, value: ScriptValue) {
        when (value) {
            is ScriptValue.Str -> luaInstance.push(value.value)
            is ScriptValue.Num -> luaInstance.push(value.value)
            is ScriptValue.Bool -> luaInstance.push(value.value)
            is ScriptValue.Nil -> luaInstance.pushNil()
            is ScriptValue.MapVal -> {
                luaInstance.createTable(0, value.value.size)
                value.value.forEach { (key, mapValue) ->
                    luaInstance.push(key)
                    pushScriptValueToLua(luaInstance, mapValue)
                    luaInstance.setTable(-3)
                }
            }
            is ScriptValue.ListVal -> {
                luaInstance.createTable(value.value.size, 0)
                value.value.forEachIndexed { index, listValue ->
                    pushScriptValueToLua(luaInstance, listValue)
                    luaInstance.rawSetI(-2, index + 1)
                }
            }
        }
    }

    private fun toScriptValue(luaInstance: Lua, index: Int): ScriptValue {
        return when (luaInstance.type(index)) {
            Lua.LuaType.NIL, Lua.LuaType.NONE -> ScriptValue.Nil
            Lua.LuaType.BOOLEAN -> ScriptValue.Bool(luaInstance.toBoolean(index))
            Lua.LuaType.NUMBER -> ScriptValue.Num(luaInstance.toNumber(index))
            Lua.LuaType.STRING -> ScriptValue.Str(luaInstance.toString(index) ?: "")
            Lua.LuaType.TABLE -> {
                // Try to determine if it's a list or a map
                val length = luaInstance.rawLength(index)
                if (length > 0) {
                    // It's likely a list
                    val list = mutableListOf<ScriptValue>()
                    for (i in 1..length) {
                        luaInstance.rawGetI(index, i)
                        list.add(toScriptValue(luaInstance, -1))
                        luaInstance.pop(1)
                    }
                    ScriptValue.ListVal(list)
                } else {
                    // It's likely a map
                    val map = mutableMapOf<String, ScriptValue>()
                    luaInstance.pushNil()
                    while (luaInstance.next(index) != 0) {
                        val key = luaInstance.toString(-2) ?: ""
                        val value = toScriptValue(luaInstance, -1)
                        map[key] = value
                        luaInstance.pop(1)
                    }
                    ScriptValue.MapVal(map)
                }
            }
            else -> ScriptValue.Nil
        }
    }

    private fun luaValueToScriptValue(luaValue: LuaValue): ScriptValue {
        return when (luaValue.type()) {
            Lua.LuaType.NIL, Lua.LuaType.NONE -> ScriptValue.Nil
            Lua.LuaType.BOOLEAN -> ScriptValue.Bool(luaValue.toBoolean())
            Lua.LuaType.NUMBER -> ScriptValue.Num(luaValue.toNumber())
            Lua.LuaType.STRING -> ScriptValue.Str(luaValue.toString())
            else -> ScriptValue.Nil
        }
    }

    private fun scriptValueToLuaValue(luaInstance: Lua, scriptValue: ScriptValue): LuaValue {
        return when (scriptValue) {
            is ScriptValue.Str -> luaInstance.from(scriptValue.value)
            is ScriptValue.Num -> luaInstance.from(scriptValue.value)
            is ScriptValue.Bool -> luaInstance.from(scriptValue.value)
            is ScriptValue.Nil -> luaInstance.fromNull()
            is ScriptValue.MapVal -> {
                // For complex types, push to stack and get LuaValue
                pushScriptValueToLua(luaInstance, scriptValue)
                luaInstance.get()
            }
            is ScriptValue.ListVal -> {
                pushScriptValueToLua(luaInstance, scriptValue)
                luaInstance.get()
            }
        }
    }
}
