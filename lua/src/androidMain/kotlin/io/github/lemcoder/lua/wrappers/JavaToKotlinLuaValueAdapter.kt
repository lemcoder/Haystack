package io.github.lemcoder.lua.wrappers

import io.github.lemcoder.lua.Lua
import io.github.lemcoder.lua.value.LuaValue
import kotlin.reflect.KClass

class JavaToKmpLuaValueAdapter(
    private val javaValue: party.iroiro.luajava.value.LuaValue
) : LuaValue {
    override fun type() = javaValue.type()
    override fun state() = javaValue.state()
    override fun push(L: Lua?) = javaValue.push(L)
    override fun call(vararg parameters: Any?) = javaValue.call(*parameters)?.map { JavaToKmpLuaValueAdapter(it!!) }?.toTypedArray()
    override fun toJavaObject() = javaValue.toJavaObject()
    override fun toBoolean() = javaValue.toBoolean()
    override fun toInteger() = javaValue.toInteger()
    override fun toNumber() = javaValue.toNumber()
    override fun toString() = javaValue.toString()
    override fun toBuffer() = javaValue.toBuffer()?.array()
    override fun <T: Any> toProxy(targetInterface: KClass<T>?) = javaValue.toProxy(targetInterface?.java)
    override fun toProxy(interfaces: List<KClass<*>?>?, degree: Lua.Conversion?) =
        javaValue.toProxy(interfaces?.map { it?.java }?.toTypedArray(), degree)
}
