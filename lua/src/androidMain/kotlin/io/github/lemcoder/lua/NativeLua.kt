package io.github.lemcoder.lua

import party.iroiro.luajava.Lua
import party.iroiro.luajava.LuaNatives
import party.iroiro.luajava.lua55.Lua55

internal class NativeLua(
    private val lua: Lua = Lua55()
) : LuaNatives by lua.luaNatives
