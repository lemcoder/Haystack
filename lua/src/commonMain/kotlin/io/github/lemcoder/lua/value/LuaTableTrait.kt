package io.github.lemcoder.lua.value

interface LuaTableTrait : MutableMap<LuaValue?, LuaValue?> {
    /**
     * Returns the length of the array part of this Lua table.
     *
     * @return the length as would be returned by the Lua `#` operator
     */
    fun length(): Int

    /**
     * Returns the size of this Lua table.
     *
     *
     *
     * Please note that Lua does not offer a method to get the size of a table,
     * so this function will need to iterate over it to calculate the size.
     *
     *
     * @return the size
     */
    override fun size(): Int

    /**
     * Gets the value at the specified integer index from the table.
     *
     * @param i the index
     * @return `thisLuaValue[i]`
     */
    fun get(i: Int): LuaValue?

    /**
     * @param key the key, either a [LuaValue] type or any Java object
     * @return `thisLuaValue[key]`
     */
    override fun get(key: Any?): LuaValue?

    /**
     * Gets the value at the specified string key from the table.
     *
     * @param key the key
     * @return `thisLuaValue[key]`
     */
    fun get(key: String?): LuaValue?

    /**
     * Gets the value at the specified Lua value key from the table.
     *
     * @param key the key
     * @return `thisLuaValue[key]`
     */
    fun get(key: LuaValue?): LuaValue?

    /**
     * Similar to [.set]
     *
     * @param key   the key
     * @param value the value, either a [LuaValue] type or any Java object
     * @return the previous value
     */
    fun set(key: Int, value: Any?): LuaValue?

    /**
     * Similar to [.put], but handles other Java types as well
     *
     * @param key   the key, either a [LuaValue] type or any Java object
     * @param value the value, either a [LuaValue] type or any Java object
     * @return the previous value
     */
    fun set(key: Any?, value: Any?): LuaValue?

    /**
     * Performs `thisLuaValue[key] = value`
     *
     * @param key   the key
     * @param value the value
     * @return the old value
     */
    override fun put(key: LuaValue?, value: LuaValue?): LuaValue?
}