package io.github.lemcoder.lua.value

import io.github.lemcoder.lua.Lua

/**
 * Interface for functions implemented in Java.
 */
fun interface LuaFunction {
    /**
     * Implements the function body
     *
     *
     *
     * Unlike [.JFunction.__call], before actually calling this function,
     * the library converts all the arguments to [LuaValues][LuaValue] and pops them off the stack.
     *
     *
     * @param L    the Lua state
     * @param args the arguments
     * @return the return values (nullable)
     */
    fun call(
        L: Lua?,
        args: List<LuaValue?>?
    ): List<LuaValue?>?
}