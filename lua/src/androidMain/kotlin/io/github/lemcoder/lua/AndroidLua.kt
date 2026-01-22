package io.github.lemcoder.lua

import io.github.lemcoder.lua.value.LuaFunction
import io.github.lemcoder.lua.value.LuaValue
import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.LuaInstances
import party.iroiro.luajava.lua55.Lua55Consts
import party.iroiro.luajava.util.ClassUtils
import java.nio.ByteBuffer
import java.util.Objects
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

private val instances = mutableListOf<AndroidLua>()

class AndroidLua : Lua {
    private val C = NativeLua()
    private val L: Long

    init {
        instances.add(this)
        val id = instances.size - 1
        this.L = C.luaL_newstate(id)
    }

    // --- Additional methods not in the Lua interface
    // TODO test this
    fun push(function: io.github.lemcoder.lua.value.KFunction) {
        checkStack(1)
        C.luaJ_pushfunction(L, function)
    }

    @Throws(java.lang.IllegalArgumentException::class)
    fun pushJavaObject(obj: Any) {
        require(!obj.javaClass.isArray) { "Expecting non-array argument" }
        checkStack(1)
        C.luaJ_pushobject(L, obj)
    }

    @Throws(java.lang.IllegalArgumentException::class)
    fun pushJavaArray(array: Any) {
        if (array.javaClass.isArray) {
            checkStack(1)
            C.luaJ_pusharray(L, array)
        } else {
            throw java.lang.IllegalArgumentException("Expecting non-array argument")
        }
    }

    /**
     * Converts a stack index into an absolute index.
     *
     * @param index a stack index
     * @return an absolute positive stack index
     */
    fun toAbsoluteIndex(index: Int): Int {
        if (index > 0) {
            return index
        }
        if (index <= C.getRegistryIndex()) {
            return index
        }
        require(index != 0) { "Stack index should not be 0" }
        return getTop() + 1 + index
    }

    fun getKotlinError(): Throwable? {
        getGlobal(party.iroiro.luajava.Lua.GLOBAL_THROWABLE)
        val o: Any? = toKotlinObject(-1)
        pop(1)
        return o as? Throwable
    }
    // ---

    override fun checkStack(extra: Int) {
        C.lua_checkstack(L, extra)
    }

    override fun push(obj: Any?, degree: Lua.Conversion?) {
        checkStack(1)
        if (obj == null) {
            pushNil()
        } else if (obj is LuaValue) {
            obj.push(this)
        } else if (obj is LuaFunction) {
            this.push(obj)
        } else if (degree == Lua.Conversion.NONE) {
            pushJavaObjectOrArray(obj)
        } else {
            if (obj is Boolean) {
                push(obj)
            } else if (obj is String) {
                push(obj)
            } else if (obj is Int || obj is Byte || obj is Short) {
                push((obj as Number).toInt().toLong())
            } else if (obj is Char) {
                push((obj.code).toLong())
            } else if (obj is Long) {
                push(obj)
            } else if (obj is Float || obj is Double) {
                push(obj as Number)
            } else if (obj is io.github.lemcoder.lua.value.KFunction) {
                push(obj)
            } else if (degree == Lua.Conversion.SEMI) {
                pushJavaObjectOrArray(obj)
            } else  /* if (degree == Conversion.FULL) */ {
                if (obj is Class<*>) {
                    pushJavaClass(obj)
                } else if (obj is MutableMap<*, *>) {
                    push(obj)
                } else if (obj is MutableCollection<*>) {
                    push(obj)
                } else if (obj.javaClass.isArray) {
                    pushArray(obj)
                } else if (obj is ByteBuffer) {
                    push(obj)
                } else {
                    pushJavaObject(obj)
                }
            }
        }
    }

    override fun pushNil() {
        checkStack(1)
        C.lua_pushnil(L)
    }

    override fun push(bool: Boolean) {
        checkStack(1)
        C.lua_pushboolean(L, if (bool) 1 else 0)
    }

    override fun push(number: Number?) {
        checkStack(1)
        C.lua_pushnumber(L, number!!.toDouble())
    }

    override fun push(integer: Long) {
        checkStack(1)
        C.lua_pushinteger(L, integer)
    }

    override fun push(string: String?) {
        checkStack(1)
        C.luaJ_pushstring(L, string)
    }

    override fun push(buffer: ByteArray) {
        var buffer = ByteBuffer.wrap(buffer)
        checkStack(1)
        if (!buffer.isDirect) {
            val directBuffer = ByteBuffer.allocateDirect(buffer.remaining())
            directBuffer.put(buffer)
            directBuffer.flip()
            buffer = directBuffer
        }
        C.luaJ_pushlstring(L, buffer, buffer.position(), buffer.remaining())
    }

    override fun push(map: MutableMap<*, *>?) {
        checkStack(3)
        C.lua_createtable(L, 0, map!!.size)
        for (entry in map.entries) {
            push(entry.key, Lua.Conversion.FULL)
            push(entry.value, Lua.Conversion.FULL)
            C.lua_rawset(L, -3)
        }
    }

    override fun push(collection: MutableCollection<*>?) {
        checkStack(2)
        C.lua_createtable(L, collection!!.size, 0)
        var i = 1
        for (o in collection) {
            push(o, Lua.Conversion.FULL)
            C.lua_rawseti(L, -2, i)
            i++
        }
    }

    override fun pushArray(array: Any?) {
        checkStack(2)
        if (array!!.javaClass.isArray) {
            val len = java.lang.reflect.Array.getLength(array)
            C.lua_createtable(L, len, 0)
            for (i in 0..<len) {
                push(
                    java.lang.reflect.Array.get(array, i),
                    Lua.Conversion.FULL
                )
                C.lua_rawseti(L, -2, i + 1)
            }
        } else {
            throw IllegalArgumentException("Not an array")
        }
    }

    override fun push(function: KFunction<*>?) {
        checkStack(1)
        C.luaJ_pushfunction(L, function)
    }

    override fun pushKotlinClass(clazz: KClass<*>?) {
        checkStack(1)
        C.luaJ_pushclass(L, clazz)
    }

    override fun push(value: LuaValue) {
        checkStack(1)
        value.push(this)
    }

    override fun push(function: LuaFunction) {
        checkStack(1)
        push(LuaFunctionWrapper(function))
    }

    override fun toNumber(index: Int): Double {
        return C.lua_tonumber(L, index)
    }

    override fun toInteger(index: Int): Long {
        return C.lua_tointeger(L, index)
    }

    override fun toBoolean(index: Int): Boolean {
        return C.lua_toboolean(L, index) != 0
    }

    override fun toObject(index: Int): Any? {
        val type: Lua.LuaType = type(index) ?: return null

        when (type) {
            Lua.LuaType.NIL, Lua.LuaType.NONE -> return null
            Lua.LuaType.BOOLEAN -> return toBoolean(index)
            Lua.LuaType.NUMBER -> return toNumber(index)
            Lua.LuaType.STRING -> return toString(index)
            Lua.LuaType.TABLE -> return toMap(index)
            Lua.LuaType.USERDATA -> return toKotlinObject(index)
            Lua.LuaType.FUNCTION,
            Lua.LuaType.LIGHTUSERDATA,
            Lua.LuaType.THREAD -> {
                pushValue(index)
                return get()
            }
        }
    }

    override fun toObject(index: Int, clazz: KClass<*>): Any? {
        val clazz = clazz.java
        val L = this
        val type: Lua.LuaType? = L.type(index)
        if (type == Lua.LuaType.NIL) {
            require(!clazz.isPrimitive) { "Primitive not accepting null values" }
            return null
        } else if (type == Lua.LuaType.BOOLEAN) {
            if (clazz == Boolean::class.javaPrimitiveType || clazz.isAssignableFrom(Boolean::class.java)) {
                return L.toBoolean(index)
            }
        } else if (type == Lua.LuaType.STRING) {
            if (clazz.isAssignableFrom(String::class.java)) {
                return L.toString(index)
            } else if (clazz.isAssignableFrom(ByteBuffer::class.java)) {
                return L.toBuffer(index)
            }
        } else if (type == Lua.LuaType.NUMBER) {
            if (clazz.isPrimitive || Number::class.java.isAssignableFrom(clazz)) {
                val v: Number?
                if (L.isInteger(index)) {
                    v = L.toInteger(index)
                } else {
                    v = L.toNumber(index)
                }
                return convertNumber(v, clazz)
            } else if (Char::class.java == clazz) {
                return L.toNumber(index).toInt().toChar()
            } else if (Boolean::class.java == clazz) {
                return L.toNumber(index) != 0.0
            } else if (clazz == Any::class.java) {
                return L.toNumber(index)
            }
        } else if (type == Lua.LuaType.USERDATA) {
            val obj: Any? = L.toKotlinObject(index)
            if (obj != null) {
                if (clazz.isAssignableFrom(obj.javaClass)) {
                    return obj
                }
                if (Number::class.java.isAssignableFrom(obj.javaClass)) {
                    return convertNumber(obj as Number, clazz)
                }
            }
        } else if (type == Lua.LuaType.TABLE) {
            if (clazz.isAssignableFrom(MutableList::class.java)) {
                return L.toList(index)
            } else if (clazz.isArray && clazz.componentType == Any::class.java) {
                return Objects.requireNonNull(L.toList(index))?.toTypedArray<Any?>()
            } else if (clazz.isAssignableFrom(MutableMap::class.java)) {
                return L.toMap(index)
            } else if (clazz.isInterface && !clazz.isAnnotation) {
                L.pushValue(index)
                return L.createProxy(
                    arrayOf<KClass<*>>(clazz.kotlin),
                    Lua.Conversion.SEMI
                )
            }
        } else if (type == Lua.LuaType.FUNCTION) {
            val descriptor = ClassUtils.getLuaFunctionalDescriptor(clazz)
            if (descriptor != null) {
                L.pushValue(index)
                L.createTable(0, 1)
                L.insert(L.getTop() - 1)
                L.setField(-2, descriptor)
                return L.createProxy(
                    arrayOf<KClass<*>>(clazz.kotlin),
                    Lua.Conversion.SEMI
                )
            }
        }
        if (clazz.isAssignableFrom(LuaValue::class.java)) {
            L.pushValue(index)
            return L.get()
        }
        throw java.lang.IllegalArgumentException("Unable to convert to " + clazz.getName())
    }

    override fun toString(index: Int): String? {
        return C.lua_tostring(L, index)
    }

    override fun toBuffer(index: Int): ByteArray? {
        return (C.luaJ_tobuffer(L, index) as? ByteBuffer)?.array()
    }

    override fun toDirectBuffer(index: Int): ByteArray? {
        val buffer = C.luaJ_todirectbuffer(L, index) as ByteBuffer?
        return buffer?.asReadOnlyBuffer()?.array()
    }

    override fun toKotlinObject(index: Int): Any? {
        return C.luaJ_toobject(L, index)
    }

    override fun toMap(index: Int): MutableMap<*, *>? {
        var index = index
        val obj: Any? = toKotlinObject(index)
        if (obj is MutableMap<*, *>) {
            return obj
        }
        checkStack(2)
        index = toAbsoluteIndex(index)
        if (C.lua_istable(L, index) == 1) {
            C.lua_pushnil(L)
            val map: MutableMap<Any?, Any?> = HashMap()
            while (C.lua_next(L, index) != 0) {
                val k = toObject(-2)
                val v = toObject(-1)
                map[k] = v
                pop(1)
            }
            return map
        }
        return null
    }

    override fun toList(index: Int): MutableList<*>? {
        val obj: Any? = toKotlinObject(index)
        if (obj is MutableList<*>) {
            return obj
        }
        checkStack(1)
        if (C.lua_istable(L, index) == 1) {
            val length = rawLength(index)
            val list = ArrayList<Any?>()
            list.ensureCapacity(length)
            for (i in 1..length) {
                C.luaJ_rawgeti(L, index, i)
                list.add(toObject(-1))
                pop(1)
            }
            return list
        }
        return null
    }

    override fun isBoolean(index: Int): Boolean {
        return C.lua_isboolean(L, index) != 0
    }

    override fun isFunction(index: Int): Boolean {
        return C.lua_isfunction(L, index) != 0
    }

    override fun isJavaObject(index: Int): Boolean {
        return C.luaJ_isobject(L, index) != 0
    }

    override fun isNil(index: Int): Boolean {
        return C.lua_isnil(L, index) != 0
    }

    override fun isNone(index: Int): Boolean {
        return C.lua_isnone(L, index) != 0
    }

    override fun isNoneOrNil(index: Int): Boolean {
        return C.lua_isnoneornil(L, index) != 0
    }

    override fun isNumber(index: Int): Boolean {
        return C.lua_isnumber(L, index) != 0
    }

    override fun isInteger(index: Int): Boolean {
        return C.luaJ_isinteger(L, index) != 0
    }

    override fun isString(index: Int): Boolean {
        return C.lua_isstring(L, index) != 0
    }

    override fun isTable(index: Int): Boolean {
        return C.lua_istable(L, index) != 0
    }

    override fun isThread(index: Int): Boolean {
        return C.lua_isthread(L, index) != 0
    }

    override fun isUserdata(index: Int): Boolean {
        return C.lua_isuserdata(L, index) != 0
    }

    override fun type(index: Int): Lua.LuaType {
        return convertType(C.lua_type(L, index))
    }

    override fun equal(i1: Int, i2: Int): Boolean {
        return C.luaJ_compare(L, i1, i2, 0) != 0
    }

    override fun rawLength(index: Int): Int {
        /* luaJ_len might push the length on stack then pop it. */
        checkStack(1)
        return C.luaJ_len(L, index)
    }

    override fun lessThan(i1: Int, i2: Int): Boolean {
        return C.luaJ_compare(L, i1, i2, -1) != 0
    }

    override fun rawEqual(i1: Int, i2: Int): Boolean {
        return C.lua_rawequal(L, i1, i2) != 0
    }

    override fun getTop(): Int {
        return C.lua_gettop(L)
    }

    override fun setTop(index: Int) {
        C.lua_settop(L, index)
    }

    override fun insert(index: Int) {
        C.lua_insert(L, index)
    }

    override fun pop(n: Int) {
        if (n < 0 || getTop() < n) {
            throw LuaException(
                LuaException.LuaError.MEMORY,
                "invalid number of items to pop"
            )
        }
        C.lua_pop(L, n)
    }

    override fun pushValue(index: Int) {
        checkStack(1)
        C.lua_pushvalue(L, index)
    }

    override fun pushThread() {
        checkStack(1)
        C.lua_pushthread(L)
    }

    override fun remove(index: Int) {
        C.lua_remove(L, index)
    }

    override fun replace(index: Int) {
        C.lua_replace(L, index)
    }

    override fun load(script: String?) {
        checkStack(1)
        checkError(C.luaL_loadstring(L, script), false)
    }

    override fun load(buffer: ByteArray, name: String) {
        val buffer = ByteBuffer.wrap(buffer)
        if (buffer.isDirect) {
            checkStack(1)
            checkError(
                C.luaJ_loadbuffer(L, buffer, buffer.position(), buffer.remaining(), name),
                false
            )
        } else {
            throw party.iroiro.luajava.LuaException(
                party.iroiro.luajava.LuaException.LuaError.MEMORY,
                "Expecting a direct buffer"
            )
        }
    }

    override fun run(script: String?) {
        checkStack(1)
        checkError(C.luaL_dostring(L, script), true)
    }

    override fun run(buffer: ByteArray, name: String) {
        val buffer = ByteBuffer.wrap(buffer)
        if (buffer.isDirect) {
            checkStack(1)
            checkError(
                C.luaJ_dobuffer(L, buffer, buffer.position(), buffer.remaining(), name),
                true
            )
        } else {
            throw party.iroiro.luajava.LuaException(
                party.iroiro.luajava.LuaException.LuaError.MEMORY,
                "Expecting a direct buffer"
            )
        }
    }

    override fun dump(): ByteArray {
        return C.luaJ_dumptobuffer(L) as ByteArray
    }

    override fun pCall(nArgs: Int, nResults: Int) {
        checkStack(max(nResults - nArgs - 1, 0))
        checkError(C.lua_pcall(L, nArgs, nResults, 0), false)
    }

    override fun newThread(): Lua? {
        checkStack(1)
        val token: LuaInstances.Token<AbstractLua?> = AbstractLua.instances.add()
        val K = C.luaJ_newthread(L, token.id)
        val lua: AbstractLua = newThread(K, token.id, this.mainThread)
        mainThread.addSubThread(lua)
        token.setter.accept(lua)
        return lua
    }

    override fun resume(nArgs: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun status(): LuaException.LuaError {
        TODO("Not yet implemented")
    }

    override fun yield(n: Int) {
        TODO("Not yet implemented")
    }

    override fun createTable(nArr: Int, nRec: Int) {
        TODO("Not yet implemented")
    }

    override fun newTable() {
        TODO("Not yet implemented")
    }

    override fun getField(index: Int, key: String?) {
        TODO("Not yet implemented")
    }

    override fun setField(index: Int, key: String?) {
        TODO("Not yet implemented")
    }

    override fun getTable(index: Int) {
        TODO("Not yet implemented")
    }

    override fun setTable(index: Int) {
        TODO("Not yet implemented")
    }

    override fun next(n: Int): Int {
        TODO("Not yet implemented")
    }

    override fun rawGet(index: Int) {
        TODO("Not yet implemented")
    }

    override fun rawGetI(index: Int, n: Int) {
        TODO("Not yet implemented")
    }

    override fun rawSet(index: Int) {
        TODO("Not yet implemented")
    }

    override fun rawSetI(index: Int, n: Int) {
        TODO("Not yet implemented")
    }

    override fun ref(index: Int): Int {
        TODO("Not yet implemented")
    }

    override fun ref(): Int {
        TODO("Not yet implemented")
    }

    override fun refGet(ref: Int) {
        TODO("Not yet implemented")
    }

    override fun unRef(index: Int, ref: Int) {
        TODO("Not yet implemented")
    }

    override fun unref(ref: Int) {
        TODO("Not yet implemented")
    }

    override fun getGlobal(name: String?) {
        TODO("Not yet implemented")
    }

    override fun setGlobal(name: String?) {
        TODO("Not yet implemented")
    }

    override fun getMetatable(index: Int): Int {
        TODO("Not yet implemented")
    }

    override fun setMetatable(index: Int) {
        TODO("Not yet implemented")
    }

    override fun getMetaField(index: Int, field: String?): Int {
        TODO("Not yet implemented")
    }

    override fun getRegisteredMetatable(typeName: String?) {
        TODO("Not yet implemented")
    }

    override fun newRegisteredMetatable(typeName: String?): Int {
        TODO("Not yet implemented")
    }

    override fun openLibraries() {
        TODO("Not yet implemented")
    }

    override fun openLibrary(name: String?) {
        TODO("Not yet implemented")
    }

    override fun concat(n: Int) {
        TODO("Not yet implemented")
    }

    override fun gc() {
        TODO("Not yet implemented")
    }

    override fun error(message: String?) {
        TODO("Not yet implemented")
    }

    override fun createProxy(
        interfaces: Array<KClass<*>>,
        degree: Lua.Conversion
    ): Any? {
        TODO("Not yet implemented")
    }

    override val mainState: Lua?
        get() = TODO("Not yet implemented")
    override val pointer: Long
        get() = TODO("Not yet implemented")
    override val id: Int
        get() = TODO("Not yet implemented")
    override val javaError: Throwable?
        get() = TODO("Not yet implemented")

    override fun error(e: Throwable?): Int {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun get(): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun set(key: String?, value: Any?) {
        TODO("Not yet implemented")
    }

    override fun get(globalName: String?): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun register(
        name: String?,
        function: LuaFunction?
    ) {
        TODO("Not yet implemented")
    }

    override fun eval(command: String?): Array<LuaValue?>? {
        TODO("Not yet implemented")
    }

    override fun require(module: String?): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun fromNull(): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(b: Boolean): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(n: Double): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(n: Long): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(s: String?): LuaValue? {
        TODO("Not yet implemented")
    }

    override fun from(buffer: ByteArray?): LuaValue? {
        TODO("Not yet implemented")
    }


    private fun convertNumber(i: Number, clazz: Class<*>): Any {
        if (clazz.isPrimitive) {
            if (Boolean::class.javaPrimitiveType == clazz) {
                return i.toInt() != 0
            }
            if (Char::class.javaPrimitiveType == clazz) {
                return Char(i.toByte().toUShort())
            } else if (Byte::class.javaPrimitiveType == clazz) {
                return i.toByte()
            } else if (Short::class.javaPrimitiveType == clazz) {
                return i.toShort()
            } else if (Int::class.javaPrimitiveType == clazz) {
                return i.toInt()
            } else if (Long::class.javaPrimitiveType == clazz) {
                return i.toLong()
            } else if (Float::class.javaPrimitiveType == clazz) {
                return i.toFloat()
            } else  /* if (double.class == clazz) */ {
                return i.toDouble()
            }
        } else {
            return convertBoxedNumber(i, clazz)
        }
    }

    @Throws(java.lang.IllegalArgumentException::class)
    private fun convertBoxedNumber(i: Number, clazz: Class<*>?): Number {
        if (Byte::class.java == clazz) {
            return i.toByte()
        } else if (Short::class.java == clazz) {
            return i.toShort()
        } else if (Int::class.java == clazz) {
            return i.toInt()
        } else if (Long::class.java == clazz) {
            return i.toLong()
        } else if (Float::class.java == clazz) {
            return i.toFloat()
        } else if (Double::class.java == clazz) {
            return i.toDouble()
        }
        throw java.lang.IllegalArgumentException("Unsupported conversion")
    }

    private class LuaFunctionWrapper(private val function: LuaFunction) :
        io.github.lemcoder.lua.value.KFunction {
        override fun __call(L: Lua?): Int {
            checkNotNull(L)
            val args = arrayOfNulls<LuaValue?>(L.getTop())
            for (i in args.indices) {
                args[args.size - i - 1] = L.get()
            }
            val results: List<LuaValue?> = function.call(L, args.toList())!!
            for (result in results) {
                L.push(result)
            }
            return results.size
        }
    }

    fun convertType(code: Int): Lua.LuaType {
        when (code) {
            Lua55Consts.LUA_TBOOLEAN -> return Lua.LuaType.BOOLEAN
            Lua55Consts.LUA_TFUNCTION -> return Lua.LuaType.FUNCTION
            Lua55Consts.LUA_TLIGHTUSERDATA -> return Lua.LuaType.LIGHTUSERDATA
            Lua55Consts.LUA_TNIL -> return Lua.LuaType.NIL
            Lua55Consts.LUA_TNONE -> return Lua.LuaType.NONE
            Lua55Consts.LUA_TNUMBER -> return Lua.LuaType.NUMBER
            Lua55Consts.LUA_TSTRING -> return Lua.LuaType.STRING
            Lua55Consts.LUA_TTABLE -> return Lua.LuaType.TABLE
            Lua55Consts.LUA_TTHREAD -> return Lua.LuaType.THREAD
            Lua55Consts.LUA_TUSERDATA -> return Lua.LuaType.USERDATA
            else -> throw LuaException(
                LuaException.LuaError.RUNTIME,
                "Unrecognized type code"
            )
        }
    }

    @Throws(LuaException::class)
    protected fun checkError(code: Int, runtime: Boolean) {
        val error = if (runtime)
            (if (code == 0) LuaException.LuaError.OK else LuaException.LuaError.RUNTIME)
        else
            convertError(code)
        if (error == LuaException.LuaError.OK) {
            return
        }
        val message: String?
        if (type(-1) == Lua.LuaType.STRING) {
            message = Objects.requireNonNull<String?>(toString(-1))
            pop(1)
        } else {
            message = "Lua-side error"
        }
        val e = LuaException(error, message!!)
        val javaError: Throwable? = getKotlinError()
        if (javaError != null) {
            e.initCause(javaError)
            error(null as Throwable?)
        }
        throw e
    }

    fun convertError(code: Int): LuaException.LuaError {
        return when (code) {
            Lua55Consts.LUA_OK -> LuaException.LuaError.OK
            Lua55Consts.LUA_YIELD -> LuaException.LuaError.YIELD
            Lua55Consts.LUA_ERRRUN -> LuaException.LuaError.RUNTIME
            Lua55Consts.LUA_ERRSYNTAX -> LuaException.LuaError.SYNTAX
            Lua55Consts.LUA_ERRMEM -> LuaException.LuaError.MEMORY
            Lua55Consts.LUA_ERRERR -> LuaException.LuaError.HANDLER
            else -> throw LuaException(
                LuaException.LuaError.RUNTIME,
                "Unrecognized error code"
            )
        }
    }
}