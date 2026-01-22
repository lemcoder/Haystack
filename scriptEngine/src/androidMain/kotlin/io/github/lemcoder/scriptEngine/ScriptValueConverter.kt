package io.github.lemcoder.scriptEngine

import party.iroiro.luajava.Lua
import party.iroiro.luajava.value.LuaValue

internal object ScriptValueConverter {
    fun toScriptValue(value: LuaValue): ScriptValue {
        return when (value.type()) {
            Lua.LuaType.NIL, Lua.LuaType.NONE -> ScriptValue.Nil
            Lua.LuaType.BOOLEAN -> ScriptValue.Bool(value.toBoolean())
            Lua.LuaType.NUMBER -> ScriptValue.Num(value.toNumber())
            Lua.LuaType.STRING -> ScriptValue.Str(value.toString())
            else -> ScriptValue.Nil
        }
    }
}