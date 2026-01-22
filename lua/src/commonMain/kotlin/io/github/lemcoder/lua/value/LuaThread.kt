package io.github.lemcoder.lua.value

import io.github.lemcoder.lua.LuaException


interface LuaThread {
    /**
     * Sets a global variable to the given value
     *
     * @param key the global variable name
     * @param value the value
     */
    fun set(key: String?, value: Any?)

    /**
     * Gets a references to a global object
     *
     * @param globalName the global name
     * @return a reference to the value
     */
    fun get(globalName: String?): LuaValue?

    /**
     * Registers the function to a global name
     *
     * @param name     the global name
     * @param function the function
     */
    fun register(name: String?, function: LuaFunction?)

    /**
     * Executes Lua code
     *
     * @param command the command
     * @return the return values
     */
    @Throws(LuaException::class)
    fun eval(command: String?): Array<LuaValue?>?

    /**
     * Loads a module, similar to the Lua `require` function
     *
     *
     *
     * Please note that this method will attempt to call
     * [Lua.openLibrary(&quot;package&quot;)][Lua.openLibrary] first and
     * cache the global `require` function.
     *
     *
     * @param module the module name
     * @return the module
     */
    @Throws(LuaException::class)
    fun require(module: String?): LuaValue?

    /**
     * Creates a nil Lua 
     *
     * @return a nil Lua value
     */
    fun fromNull(): LuaValue?

    /**
     * Creates a boolean Lua value from a Java boolean.
     *
     * @param b the boolean
     * @return a boolean Lua value
     */
    fun from(b: Boolean): LuaValue?

    /**
     * Creates a number Lua value from a Java double.
     *
     * @param n the number
     * @return a number Lua value
     */
    fun from(n: Double): LuaValue?

    /**
     * Creates a number Lua value from a Java long.
     *
     * @param n the number
     * @return a number Lua value
     */
    fun from(n: Long): LuaValue?

    /**
     * Creates a string Lua value from a Java string.
     *
     * @param s the string
     * @return a string Lua value
     */
    fun from(s: String?): LuaValue?

    /**
     * Creates a raw string Lua value from a byte buffer.
     *
     * @param buffer the content of the raw string
     * @return a raw string Lua value
     */
    fun from(buffer: ByteArray?): LuaValue?
}