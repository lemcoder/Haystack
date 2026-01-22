package io.github.lemcoder.lua.value

import io.github.lemcoder.lua.Lua
import io.github.lemcoder.lua.LuaException
import kotlin.reflect.KClass

interface LuaValue : LuaTableTrait {
    /**
     * Returns the type of this Lua value.
     *
     * @return the type of the Lua value
     */
    fun type(): Lua.LuaType

    /**
     * Returns the Lua state where this value resides.
     *
     * @return the Lua state where this Lua value lives
     */
    fun state(): Lua?

    /**
     * Pushes the Lua value onto the Lua stack of another thread sharing the same main state
     *
     * @param L another thread
     */
    @Throws(LuaException::class)
    fun push(L: Lua?)

    /**
     * Performs `thisLuaValue(parameter1, parameter2, ...)`
     *
     * @param parameters the parameters
     * @return the return values, `null` on error
     */
    fun call(vararg parameters: Any?): Array<LuaValue?>?

    /**
     * Converts this Lua value to a Java object.
     *
     * @return a Java value converted from this Lua value
     * @see Lua.toObject
     */
    fun toJavaObject(): Any?

    fun toBoolean(): Boolean

    fun toInteger(): Long

    fun toNumber(): Double

    override fun toString(): String

    fun toBuffer(): ByteArray?

    /**
     * Creates a proxy from this value with [Lua.createProxy].
     *
     * @param <T> the type of the proxy interface
     * @param targetInterface the interfaces the proxy should implement.
     * @return the proxy object
    </T> */
    fun <T: Any> toProxy(targetInterface: KClass<T>?): T?

    /**
     * Creates a proxy from this value with [Lua.createProxy].
     *
     * @param interfaces the interfaces the proxy should implement.
     * @param degree the conversion used
     * @return the proxy object
     */
    fun toProxy(
        interfaces: List<KClass<*>?>?,
        degree: Lua.Conversion?
    ): Any?
}