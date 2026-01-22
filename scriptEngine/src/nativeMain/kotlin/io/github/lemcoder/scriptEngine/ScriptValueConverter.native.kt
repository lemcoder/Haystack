package io.github.lemcoder.scriptEngine

import kotlinx.cinterop.*
import lua.*

@OptIn(ExperimentalForeignApi::class)
internal object ScriptValueConverter {

    /** Convert a value on the Lua stack to ScriptValue */
    fun toScriptValue(L: CPointer<lua_State>, index: Int): ScriptValue {
        return when (lua_type(L, index)) {
            LUA_TNIL -> ScriptValue.Nil
            LUA_TBOOLEAN -> ScriptValue.Bool(lua_toboolean(L, index) != 0)
            LUA_TNUMBER -> ScriptValue.Num(LuaMacros.lua_tonumber(L, index))
            LUA_TSTRING -> {
                val str = LuaMacros.lua_tostring(L, index)?.toKString() ?: ""
                ScriptValue.Str(str)
            }
            LUA_TTABLE -> convertTable(L, index)
            else -> ScriptValue.Nil
        }
    }

    /** Push a ScriptValue onto the Lua stack */
    fun pushToLua(L: CPointer<lua_State>, value: ScriptValue) {
        when (value) {
            is ScriptValue.Str -> lua_pushstring(L, value.value)
            is ScriptValue.Num -> lua_pushnumber(L, value.value)
            is ScriptValue.Bool -> lua_pushboolean(L, if (value.value) 1 else 0)
            is ScriptValue.Nil -> lua_pushnil(L)
            is ScriptValue.MapVal -> {
                lua_createtable(L, 0, value.value.size)
                value.value.forEach { (key, mapValue) ->
                    lua_pushstring(L, key)
                    pushToLua(L, mapValue)
                    lua_settable(L, -3)
                }
            }
            is ScriptValue.ListVal -> {
                lua_createtable(L, value.value.size, 0)
                value.value.forEachIndexed { index, listValue ->
                    pushToLua(L, listValue)
                    lua_rawseti(L, -2, (index + 1).toLong())
                }
            }
        }
    }

    /** Convert a Lua table to either ListVal or MapVal */
    private fun convertTable(L: CPointer<lua_State>, index: Int): ScriptValue {
        // Normalize index to absolute position
        val absoluteIndex =
            if (index < 0) {
                lua_gettop(L) + index + 1
            } else {
                index
            }

        // Try to determine if it's a list or a map
        val length = lua_rawlen(L, absoluteIndex).toInt()

        return if (length > 0) {
            // It's likely an array - convert to ListVal
            val list = mutableListOf<ScriptValue>()
            for (i in 1..length) {
                lua_rawgeti(L, absoluteIndex, i.toLong())
                list.add(toScriptValue(L, -1))
                LuaMacros.lua_pop(L, 1)
            }
            ScriptValue.ListVal(list)
        } else {
            // It's a map - convert to MapVal
            val map = mutableMapOf<String, ScriptValue>()
            lua_pushnil(L)
            while (lua_next(L, absoluteIndex) != 0) {
                val key = LuaMacros.lua_tostring(L, -2)?.toKString() ?: ""
                val value = toScriptValue(L, -1)
                map[key] = value
                LuaMacros.lua_pop(L, 1)
            }
            ScriptValue.MapVal(map)
        }
    }
}
