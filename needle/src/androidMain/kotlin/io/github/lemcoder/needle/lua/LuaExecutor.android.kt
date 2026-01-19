package io.github.lemcoder.needle.lua

import party.iroiro.luajava.lua55.Lua55

actual fun createLuaExecutor(): LuaExecutor {
    return AndroidLuaExecutor()
}

internal class AndroidLuaExecutor : LuaExecutor {
    override fun <OUT> run(code: String, args: Map<String, Any?>): OUT? {
        try {
            Lua55().use {
                it.run("34 + 35")
            }
        } catch(e: Exception) {

        }
                return null
    }
}