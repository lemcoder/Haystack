package io.github.lemcoder.lua

import io.github.lemcoder.lua.value.LuaFunction
import io.github.lemcoder.lua.value.LuaValue
import party.iroiro.luajava.lua55.Lua55
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

actual fun getLua(): Lua = AndroidLua()

private class AndroidLua: Lua {
    private val lua = Lua55()
    override fun checkStack(extra: Int)  = lua.checkStack(extra)

    override fun push(`object`: Any?, degree: Lua.Conversion?) {
        val javaConversion = degree?.toJavaConversion() ?: party.iroiro.luajava.Lua.Conversion.NONE
        lua.push(`object`, javaConversion)
    }

    override fun pushNil() = lua.pushNil()

    override fun push(bool: Boolean) = lua.push(bool)

    override fun push(number: Number?) {
        number?.let { lua.push(it) } ?: lua.pushNil()
    }

    override fun push(integer: Long) = lua.push(integer)

    override fun push(string: String?) {
        string?.let { lua.push(it) } ?: lua.pushNil()
    }

    override fun push(buffer: ByteArray?) {
        buffer?.let { lua.push(ByteBuffer.wrap(it)) } ?: lua.pushNil()
    }

    override fun push(map: MutableMap<*, *>?) {
        map?.let { lua.push(it) } ?: lua.pushNil()
    }

    override fun push(collection: MutableCollection<*>?) {
        collection?.let { lua.push(it) } ?: lua.pushNil()
    }

    override fun pushArray(array: Any?) {
        array?.let { lua.pushArray(it) } ?: lua.pushNil()
    }

    override fun push(function: KFunction<*>?) {
        function?.let { 
            // KFunction needs to be wrapped as a JFunction
            TODO("KFunction conversion not yet implemented")
        } ?: lua.pushNil()
    }

    override fun pushKotlinClass(clazz: KClass<*>?) {
        clazz?.java?.let { lua.pushJavaClass(it) } ?: lua.pushNil()
    }

    override fun push(value: LuaValue?) {
        value?.let {
            // Cast to the Java library's LuaValue type
            lua.push(it as party.iroiro.luajava.value.LuaValue)
        } ?: lua.pushNil()
    }

    override fun push(value: LuaFunction?) {
        value?.let {
            // Cast to the Java library's LuaFunction type
            lua.push(it as party.iroiro.luajava.value.LuaFunction)
        } ?: lua.pushNil()
    }

    override fun pushJavaObject(`object`: Any?) {
        `object`?.let { lua.pushJavaObject(it) } ?: lua.pushNil()
    }

    override fun pushJavaArray(array: Any?) {
        array?.let { lua.pushJavaArray(it) } ?: lua.pushNil()
    }

    override fun toNumber(index: Int): Double = lua.toNumber(index)

    override fun toInteger(index: Int): Long = lua.toInteger(index)

    override fun toBoolean(index: Int): Boolean = lua.toBoolean(index)

    override fun toObject(index: Int): Any? = lua.toObject(index)

    override fun toObject(index: Int, type: KClass<*>?): Any? {
        return type?.java?.let { lua.toObject(index, it) }
    }

    override fun toString(index: Int): String? = lua.toString(index)

    override fun toBuffer(index: Int): ByteArray? {
        return lua.toBuffer(index)?.let {
            val array = ByteArray(it.remaining())
            it.get(array)
            array
        }
    }

    override fun toDirectBuffer(index: Int): ByteArray? {
        return lua.toDirectBuffer(index)?.let {
            val array = ByteArray(it.remaining())
            it.get(array)
            array
        }
    }

    override fun toJavaObject(index: Int): Any? = lua.toJavaObject(index)

    override fun toMap(index: Int): MutableMap<*, *>? = lua.toMap(index)

    override fun toList(index: Int): MutableList<*>? = lua.toList(index)

    override fun isBoolean(index: Int): Boolean = lua.isBoolean(index)

    override fun isFunction(index: Int): Boolean = lua.isFunction(index)

    override fun isJavaObject(index: Int): Boolean = lua.isJavaObject(index)

    override fun isNil(index: Int): Boolean = lua.isNil(index)

    override fun isNone(index: Int): Boolean = lua.isNone(index)

    override fun isNoneOrNil(index: Int): Boolean = lua.isNoneOrNil(index)

    override fun isNumber(index: Int): Boolean = lua.isNumber(index)

    override fun isInteger(index: Int): Boolean = lua.isInteger(index)

    override fun isString(index: Int): Boolean = lua.isString(index)

    override fun isTable(index: Int): Boolean = lua.isTable(index)

    override fun isThread(index: Int): Boolean = lua.isThread(index)

    override fun isUserdata(index: Int): Boolean = lua.isUserdata(index)

    override fun type(index: Int): Lua.LuaType? = lua.type(index)?.toKotlinLuaType()

    override fun equal(i1: Int, i2: Int): Boolean = lua.equal(i1, i2)

    override fun rawLength(index: Int): Int = lua.rawLength(index)

    override fun lessThan(i1: Int, i2: Int): Boolean = lua.lessThan(i1, i2)

    override fun rawEqual(i1: Int, i2: Int): Boolean = lua.rawEqual(i1, i2)

    override var top: Int
        get() = lua.top
        set(value) { lua.top = value }

    override fun insert(index: Int) = lua.insert(index)

    override fun pop(n: Int) = lua.pop(n)

    override fun pushValue(index: Int) = lua.pushValue(index)

    override fun pushThread() = lua.pushThread()

    override fun remove(index: Int) = lua.remove(index)

    override fun replace(index: Int) = lua.replace(index)

    override fun xMove(other: Lua?, n: Int) {
        // Attempt to cast other to party.iroiro.luajava.Lua
        other?.let {
            if (it is AndroidLua) {
                lua.xMove(it.lua, n)
            } else {
                throw IllegalArgumentException("Cannot move values between different Lua implementations")
            }
        }
    }

    override fun load(script: String?) {
        script?.let { lua.load(it) }
    }

    override fun load(buffer: ByteArray?, name: String?) {
        if (buffer != null && name != null) {
            lua.load(ByteBuffer.wrap(buffer), name)
        }
    }

    override fun run(script: String?) {
        script?.let { lua.run(it) }
    }

    override fun run(buffer: ByteArray?, name: String?) {
        if (buffer != null && name != null) {
            lua.run(ByteBuffer.wrap(buffer), name)
        }
    }

    override fun dump(): ByteArray? {
        return lua.dump()?.let {
            val array = ByteArray(it.remaining())
            it.get(array)
            array
        }
    }

    override fun pCall(nArgs: Int, nResults: Int) = lua.pCall(nArgs, nResults)

    override fun newThread(): Lua? {
        // Wrap the returned thread
        return AndroidLua().apply {
            // TODO: Need to properly initialize with the new thread
        }
    }

    override fun resume(nArgs: Int): Boolean = lua.resume(nArgs)

    override fun status(): LuaException.LuaError? = lua.status().toKotlinLuaError()

    override fun yield(n: Int) = lua.yield(n)

    override fun createTable(nArr: Int, nRec: Int) = lua.createTable(nArr, nRec)

    override fun newTable() = lua.newTable()

    override fun getField(index: Int, key: String?) {
        key?.let { lua.getField(index, it) }
    }

    override fun setField(index: Int, key: String?) {
        key?.let { lua.setField(index, it) }
    }

    override fun getTable(index: Int) = lua.getTable(index)

    override fun setTable(index: Int) = lua.setTable(index)

    override fun next(n: Int): Int = lua.next(n)

    override fun rawGet(index: Int) = lua.rawGet(index)

    override fun rawGetI(index: Int, n: Int) = lua.rawGetI(index, n)

    override fun rawSet(index: Int) = lua.rawSet(index)

    override fun rawSetI(index: Int, n: Int) = lua.rawSetI(index, n)

    override fun ref(index: Int): Int = lua.ref(index)

    override fun ref(): Int = lua.ref()

    override fun refGet(ref: Int) = lua.refGet(ref)

    override fun unRef(index: Int, ref: Int) = lua.unRef(index, ref)

    override fun unref(ref: Int) = lua.unref(ref)

    override fun getGlobal(name: String?) {
        name?.let { lua.getGlobal(it) }
    }

    override fun setGlobal(name: String?) {
        name?.let { lua.setGlobal(it) }
    }

    override fun getMetatable(index: Int): Int = lua.getMetatable(index)

    override fun setMetatable(index: Int) = lua.setMetatable(index)

    override fun getMetaField(index: Int, field: String?): Int {
        return field?.let { lua.getMetaField(index, it) } ?: 0
    }

    override fun getRegisteredMetatable(typeName: String?) {
        typeName?.let { lua.getRegisteredMetatable(it) }
    }

    override fun newRegisteredMetatable(typeName: String?): Int {
        return typeName?.let { lua.newRegisteredMetatable(it) } ?: 0
    }

    override fun openLibraries() = lua.openLibraries()

    override fun openLibrary(name: String?) {
        name?.let { lua.openLibrary(it) }
    }

    override fun concat(n: Int) = lua.concat(n)

    override fun gc() = lua.gc()

    override fun error(message: String?) {
        message?.let { lua.error(it) }
    }

    override fun createProxy(
        interfaces: Array<KClass<*>?>?,
        degree: Lua.Conversion?
    ): Any? {
        val javaClasses = interfaces?.mapNotNull { it?.java }?.toTypedArray() ?: arrayOf()
        val javaConversion = degree?.toJavaConversion() ?: party.iroiro.luajava.Lua.Conversion.NONE
        return lua.createProxy(javaClasses, javaConversion)
    }

    override val mainState: Lua?
        get() {
            // Wrap the main state if needed
            return lua.mainState?.let { AndroidLua().apply { /* TODO: init with main state */ } }
        }
    override val pointer: Long
        get() = lua.pointer
    override val id: Int
        get() = lua.id
    override val javaError: Throwable?
        get() = lua.javaError

    override fun error(e: Throwable?): Int = lua.error(e)

    override fun close() = lua.close()

    override fun get(): LuaValue? = lua.get() as? LuaValue

    override fun set(key: String?, value: Any?) {
        if (key != null && value != null) {
            lua.set(key, value)
        }
    }

    override fun get(globalName: String?): LuaValue? {
        return globalName?.let { lua.get(it) as? LuaValue }
    }

    override fun register(
        name: String?,
        function: LuaFunction?
    ) {
        if (name != null && function != null) {
            lua.register(name, function as party.iroiro.luajava.value.LuaFunction)
        }
    }

    override fun eval(command: String?): Array<LuaValue?>? {
        return command?.let { 
            lua.eval(it).map { it as? LuaValue }.toTypedArray() 
        }
    }

    override fun require(module: String?): LuaValue? {
        return module?.let { lua.require(it) as? LuaValue }
    }

    override fun fromNull(): LuaValue? = lua.fromNull() as? LuaValue

    override fun from(b: Boolean): LuaValue? = lua.from(b) as? LuaValue

    override fun from(n: Double): LuaValue? = lua.from(n) as? LuaValue

    override fun from(n: Long): LuaValue? = lua.from(n) as? LuaValue

    override fun from(s: String?): LuaValue? {
        return s?.let { lua.from(it) as? LuaValue }
    }

    override fun from(buffer: ByteArray?): LuaValue? {
        return buffer?.let { lua.from(ByteBuffer.wrap(it)) as? LuaValue }
    }
}

// Extension functions for type conversions
private fun Lua.Conversion.toJavaConversion(): party.iroiro.luajava.Lua.Conversion {
    return when (this) {
        Lua.Conversion.FULL -> party.iroiro.luajava.Lua.Conversion.FULL
        Lua.Conversion.SEMI -> party.iroiro.luajava.Lua.Conversion.SEMI
        Lua.Conversion.NONE -> party.iroiro.luajava.Lua.Conversion.NONE
    }
}

private fun party.iroiro.luajava.Lua.LuaType.toKotlinLuaType(): Lua.LuaType? {
    return when (this) {
        party.iroiro.luajava.Lua.LuaType.BOOLEAN -> Lua.LuaType.BOOLEAN
        party.iroiro.luajava.Lua.LuaType.FUNCTION -> Lua.LuaType.FUNCTION
        party.iroiro.luajava.Lua.LuaType.LIGHTUSERDATA -> Lua.LuaType.LIGHTUSERDATA
        party.iroiro.luajava.Lua.LuaType.NIL -> Lua.LuaType.NIL
        party.iroiro.luajava.Lua.LuaType.NONE -> Lua.LuaType.NONE
        party.iroiro.luajava.Lua.LuaType.NUMBER -> Lua.LuaType.NUMBER
        party.iroiro.luajava.Lua.LuaType.STRING -> Lua.LuaType.STRING
        party.iroiro.luajava.Lua.LuaType.TABLE -> Lua.LuaType.TABLE
        party.iroiro.luajava.Lua.LuaType.THREAD -> Lua.LuaType.THREAD
        party.iroiro.luajava.Lua.LuaType.USERDATA -> Lua.LuaType.USERDATA
    }
}

private fun party.iroiro.luajava.LuaException.LuaError.toKotlinLuaError(): LuaException.LuaError {
    return when (this) {
        party.iroiro.luajava.LuaException.LuaError.OK -> LuaException.LuaError.OK
        party.iroiro.luajava.LuaException.LuaError.YIELD -> LuaException.LuaError.YIELD
        party.iroiro.luajava.LuaException.LuaError.RUNTIME -> LuaException.LuaError.RUNTIME
        party.iroiro.luajava.LuaException.LuaError.SYNTAX -> LuaException.LuaError.SYNTAX
        party.iroiro.luajava.LuaException.LuaError.MEMORY -> LuaException.LuaError.MEMORY
        party.iroiro.luajava.LuaException.LuaError.FILE -> LuaException.LuaError.FILE
        party.iroiro.luajava.LuaException.LuaError.GC -> LuaException.LuaError.GC
        party.iroiro.luajava.LuaException.LuaError.HANDLER -> LuaException.LuaError.HANDLER
        party.iroiro.luajava.LuaException.LuaError.UNKNOWN -> LuaException.LuaError.UNKNOWN
        party.iroiro.luajava.LuaException.LuaError.JAVA -> LuaException.LuaError.JAVA
    }
}