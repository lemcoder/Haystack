package io.github.lemcoder.scriptEngine

import kotlinx.cinterop.*
import lua.*

/**
 * Helper functions to replace Lua C macros that aren't automatically exposed through cinterop
 */
@OptIn(ExperimentalForeignApi::class)
object LuaMacros {
    
    // #define lua_pop(L,n) lua_settop(L, -(n)-1)
    fun lua_pop(L: CPointer<lua_State>, n: Int) {
        lua_settop(L, -(n) - 1)
    }
    
    // #define lua_tostring(L,i) lua_tolstring(L, (i), NULL)
    fun lua_tostring(L: CPointer<lua_State>, index: Int): CPointer<ByteVar>? {
        return lua_tolstring(L, index, null)
    }
    
    // #define lua_tonumber(L,i) lua_tonumberx(L,(i),NULL)
    fun lua_tonumber(L: CPointer<lua_State>, index: Int): lua_Number {
        return lua_tonumberx(L, index, null)
    }
    
    // #define lua_pcall(L,n,r,f) lua_pcallk(L, (n), (r), (f), 0, NULL)
    fun lua_pcall(L: CPointer<lua_State>, nargs: Int, nresults: Int, errfunc: Int): Int {
        return lua_pcallk(L, nargs, nresults, errfunc, 0, null)
    }
    
    // #define lua_upvalueindex(i) (LUA_REGISTRYINDEX - (i))
    fun lua_upvalueindex(i: Int): Int {
        return LUA_REGISTRYINDEX - i
    }
    
    // #define luaL_openlibs(L) luaL_openselectedlibs(L, ~0, 0)
    fun luaL_openlibs(L: CPointer<lua_State>) {
        luaL_openselectedlibs(L, -1, 0) // ~0 is -1 in Kotlin
    }
}
