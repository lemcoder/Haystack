package io.github.lemcoder.scriptEngine

import party.iroiro.luajava.Lua
import party.iroiro.luajava.value.LuaValue

internal object ScriptValueConverter {
    /** Convert a LuaValue to ScriptValue (used when lua.eval returns results) */
    fun toScriptValue(value: LuaValue): ScriptValue {
        return when (value.type()) {
            Lua.LuaType.NIL,
            Lua.LuaType.NONE -> ScriptValue.Nil
            Lua.LuaType.BOOLEAN -> ScriptValue.Bool(value.toBoolean())
            Lua.LuaType.NUMBER -> ScriptValue.Num(value.toNumber())
            Lua.LuaType.STRING -> ScriptValue.Str(value.toString())
            else -> ScriptValue.Nil
        }
    }

    /** Convert a value on the Lua stack to ScriptValue (handles tables properly) */
    fun toScriptValue(luaInstance: Lua, index: Int): ScriptValue {
        return when (luaInstance.type(index)) {
            Lua.LuaType.NIL,
            Lua.LuaType.NONE -> ScriptValue.Nil
            Lua.LuaType.BOOLEAN -> ScriptValue.Bool(luaInstance.toBoolean(index))
            Lua.LuaType.NUMBER -> ScriptValue.Num(luaInstance.toNumber(index))
            Lua.LuaType.STRING -> ScriptValue.Str(luaInstance.toString(index) ?: "")
            Lua.LuaType.TABLE -> convertTable(luaInstance, index)
            else -> ScriptValue.Nil
        }
    }

    /** Convert a ScriptValue to LuaValue */
    fun toLuaValue(luaInstance: Lua, scriptValue: ScriptValue): LuaValue {
        return when (scriptValue) {
            is ScriptValue.Str -> luaInstance.from(scriptValue.value)
            is ScriptValue.Num -> luaInstance.from(scriptValue.value)
            is ScriptValue.Bool -> luaInstance.from(scriptValue.value)
            is ScriptValue.Nil -> luaInstance.fromNull()
            is ScriptValue.MapVal -> {
                // For complex types, push to stack and get LuaValue
                pushToLua(luaInstance, scriptValue)
                luaInstance.get()
            }
            is ScriptValue.ListVal -> {
                pushToLua(luaInstance, scriptValue)
                luaInstance.get()
            }
        }
    }

    /** Push a ScriptValue onto the Lua stack */
    fun pushToLua(luaInstance: Lua, value: ScriptValue) {
        when (value) {
            is ScriptValue.Str -> luaInstance.push(value.value)
            is ScriptValue.Num -> luaInstance.push(value.value)
            is ScriptValue.Bool -> luaInstance.push(value.value)
            is ScriptValue.Nil -> luaInstance.pushNil()
            is ScriptValue.MapVal -> {
                luaInstance.createTable(0, value.value.size)
                value.value.forEach { (key, mapValue) ->
                    luaInstance.push(key)
                    pushToLua(luaInstance, mapValue)
                    luaInstance.setTable(-3)
                }
            }
            is ScriptValue.ListVal -> {
                luaInstance.createTable(value.value.size, 0)
                value.value.forEachIndexed { index, listValue ->
                    pushToLua(luaInstance, listValue)
                    luaInstance.rawSetI(-2, index + 1)
                }
            }
        }
    }

    /** Convert a Lua table to either ListVal or MapVal */
    private fun convertTable(luaInstance: Lua, index: Int): ScriptValue {
        // Normalize index to absolute position (negative indices are relative to stack top)
        val absoluteIndex =
            if (index < 0) {
                luaInstance.top + index + 1
            } else {
                index
            }

        // Try to determine if it's a list or a map
        val length = luaInstance.rawLength(absoluteIndex)

        return if (length > 0) {
            // It's likely an array - convert to ListVal
            val list = mutableListOf<ScriptValue>()
            for (i in 1..length) {
                luaInstance.rawGetI(absoluteIndex, i)
                list.add(toScriptValue(luaInstance, -1))
                luaInstance.pop(1)
            }
            ScriptValue.ListVal(list)
        } else {
            // It's a map - convert to MapVal
            val map = mutableMapOf<String, ScriptValue>()
            luaInstance.pushNil()
            while (luaInstance.next(absoluteIndex) != 0) {
                val key = luaInstance.toString(-2) ?: ""
                val value = toScriptValue(luaInstance, -1)
                map[key] = value
                luaInstance.pop(1)
            }
            ScriptValue.MapVal(map)
        }
    }
}
