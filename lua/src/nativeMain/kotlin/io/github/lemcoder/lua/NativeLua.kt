package io.github.lemcoder.lua

import io.github.lemcoder.lua.value.LuaFunction
import io.github.lemcoder.lua.value.LuaValue
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.rawValue
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import lua.LUA_ERRERR
import lua.LUA_ERRFILE
import lua.LUA_ERRMEM
import lua.LUA_ERRRUN
import lua.LUA_ERRSYNTAX
import lua.LUA_GCCOLLECT
import lua.LUA_MULTRET
import lua.LUA_OK
import lua.LUA_OPEQ
import lua.LUA_OPLT
import lua.LUA_REGISTRYINDEX
import lua.LUA_TBOOLEAN
import lua.LUA_TFUNCTION
import lua.LUA_TLIGHTUSERDATA
import lua.LUA_TNIL
import lua.LUA_TNONE
import lua.LUA_TNUMBER
import lua.LUA_TSTRING
import lua.LUA_TTABLE
import lua.LUA_TTHREAD
import lua.LUA_TUSERDATA
import lua.LUA_YIELD
import lua.luaL_getmetafield
import lua.luaL_loadbufferx
import lua.luaL_loadstring
import lua.luaL_newmetatable
import lua.luaL_newstate
import lua.luaL_openselectedlibs
import lua.luaL_ref
import lua.luaL_requiref
import lua.luaL_unref
import lua.lua_State
import lua.lua_checkstack
import lua.lua_close
import lua.lua_compare
import lua.lua_concat
import lua.lua_copy
import lua.lua_createtable
import lua.lua_error
import lua.lua_gc
import lua.lua_getfield
import lua.lua_getglobal
import lua.lua_getmetatable
import lua.lua_gettable
import lua.lua_gettop
import lua.lua_iscfunction
import lua.lua_isinteger
import lua.lua_isnumber
import lua.lua_isstring
import lua.lua_isuserdata
import lua.lua_newthread
import lua.lua_next
import lua.lua_pcallk
import lua.lua_pushboolean
import lua.lua_pushinteger
import lua.lua_pushlstring
import lua.lua_pushnil
import lua.lua_pushnumber
import lua.lua_pushstring
import lua.lua_pushthread
import lua.lua_pushvalue
import lua.lua_rawequal
import lua.lua_rawget
import lua.lua_rawgeti
import lua.lua_rawlen
import lua.lua_rawset
import lua.lua_rawseti
import lua.lua_resume
import lua.lua_rotate
import lua.lua_setfield
import lua.lua_setglobal
import lua.lua_setmetatable
import lua.lua_settable
import lua.lua_settop
import lua.lua_status
import lua.lua_toboolean
import lua.lua_tointegerx
import lua.lua_tolstring
import lua.lua_tonumberx
import lua.lua_type
import lua.lua_xmove
import platform.posix.size_tVar
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

@OptIn(ExperimentalForeignApi::class)
internal class NativeLua : Lua {
    private val L: CPointer<lua_State>? = luaL_newstate()
    private var closed = false

    init {
        require(L != null) { "Failed to create Lua state" }
    }

    private fun checkNotClosed() {
        if (closed) throw IllegalStateException("Lua state is closed")
    }

    override fun checkStack(extra: Int) {
        checkNotClosed()
        if (lua_checkstack(L, extra) == 0) {
            throw RuntimeException("Unable to grow stack")
        }
    }

    override fun push(`object`: Any?, degree: Lua.Conversion?) {
        checkNotClosed()
        when {
            `object` == null -> pushNil()
            `object` is Boolean -> push(`object`)
            `object` is Number -> push(`object`)
            `object` is String -> push(`object`)
            `object` is ByteArray -> push(`object`)
            `object` is Long -> push(`object`)
            `object` is Int -> push(`object`.toLong())
            `object` is Double -> push(`object`)
            `object` is Float -> push(`object`.toDouble())
            degree == Lua.Conversion.FULL -> {
                when (`object`) {
                    is Map<*, *> -> push(`object` as MutableMap<*, *>)
                    is Collection<*> -> push(`object` as MutableCollection<*>)
                    is Array<*> -> pushArray(`object`)
                    else -> pushJavaObject(`object`)
                }
            }
            else -> pushJavaObject(`object`)
        }
    }

    override fun pushNil() {
        checkNotClosed()
        lua_pushnil(L)
    }

    override fun push(bool: Boolean) {
        checkNotClosed()
        lua_pushboolean(L, if (bool) 1 else 0)
    }

    override fun push(number: Number?) {
        checkNotClosed()
        if (number == null) {
            pushNil()
        } else {
            lua_pushnumber(L, number.toDouble())
        }
    }

    override fun push(integer: Long) {
        checkNotClosed()
        lua_pushinteger(L, integer)
    }

    override fun push(string: String?) {
        checkNotClosed()
        if (string == null) {
            pushNil()
        } else {
            lua_pushstring(L, string)
        }
    }

    override fun push(buffer: ByteArray?) {
        checkNotClosed()
        if (buffer == null) {
            pushNil()
        } else {
            lua_pushlstring(L, buffer.decodeToString(), buffer.size.convert())
        }
    }

    override fun push(map: MutableMap<*, *>?) {
        checkNotClosed()
        if (map == null) {
            pushNil()
        } else {
            createTable(0, map.size)
            map.forEach { (key, value) ->
                push(key, Lua.Conversion.FULL)
                push(value, Lua.Conversion.FULL)
                setTable(-3)
            }
        }
    }

    override fun push(collection: MutableCollection<*>?) {
        checkNotClosed()
        if (collection == null) {
            pushNil()
        } else {
            createTable(collection.size, 0)
            collection.forEachIndexed { index, value ->
                push(value, Lua.Conversion.FULL)
                rawSetI(-2, index + 1)
            }
        }
    }

    override fun pushArray(array: Any?) {
        checkNotClosed()
        if (array == null) {
            pushNil()
        } else {
            when (array) {
                is Array<*> -> {
                    createTable(array.size, 0)
                    array.forEachIndexed { index, value ->
                        push(value, Lua.Conversion.FULL)
                        rawSetI(-2, index + 1)
                    }
                }
                is IntArray -> {
                    createTable(array.size, 0)
                    array.forEachIndexed { index, value ->
                        push(value.toLong())
                        rawSetI(-2, index + 1)
                    }
                }
                is LongArray -> {
                    createTable(array.size, 0)
                    array.forEachIndexed { index, value ->
                        push(value)
                        rawSetI(-2, index + 1)
                    }
                }
                is DoubleArray -> {
                    createTable(array.size, 0)
                    array.forEachIndexed { index, value ->
                        push(value)
                        rawSetI(-2, index + 1)
                    }
                }
                is FloatArray -> {
                    createTable(array.size, 0)
                    array.forEachIndexed { index, value ->
                        push(value.toDouble())
                        rawSetI(-2, index + 1)
                    }
                }
                is BooleanArray -> {
                    createTable(array.size, 0)
                    array.forEachIndexed { index, value ->
                        push(value)
                        rawSetI(-2, index + 1)
                    }
                }
                else -> throw IllegalArgumentException("Unsupported array type")
            }
        }
    }

    override fun push(function: KFunction<*>?) {
        checkNotClosed()
        if (function == null) {
            pushNil()
        } else {
            // TODO: Implement KFunction wrapping
            throw UnsupportedOperationException("KFunction conversion not yet implemented")
        }
    }

    override fun pushKotlinClass(clazz: KClass<*>?) {
        checkNotClosed()
        if (clazz == null) {
            pushNil()
        } else {
            // TODO: Implement class wrapping
            throw UnsupportedOperationException("Class conversion not yet implemented")
        }
    }

    override fun push(value: LuaValue?) {
        checkNotClosed()
        if (value == null) {
            pushNil()
        } else {
            value.push(this)
        }
    }

    override fun push(value: LuaFunction?) {
        checkNotClosed()
        if (value == null) {
            pushNil()
        } else {
            // TODO: Implement LuaFunction wrapping
            throw UnsupportedOperationException("LuaFunction conversion not yet implemented")
        }
    }

    override fun pushJavaObject(`object`: Any?) {
        checkNotClosed()
        if (`object` == null) {
            pushNil()
        } else {
            // TODO: Implement userdata wrapping for Kotlin objects
            throw UnsupportedOperationException("Java object conversion not yet implemented")
        }
    }

    override fun pushJavaArray(array: Any?) {
        checkNotClosed()
        if (array == null) {
            pushNil()
        } else {
            pushArray(array)
        }
    }

    override fun toNumber(index: Int): Double {
        checkNotClosed()
        return lua_tonumberx(L, index, null)
    }

    override fun toInteger(index: Int): Long {
        checkNotClosed()
        return lua_tointegerx(L, index, null)
    }

    override fun toBoolean(index: Int): Boolean {
        checkNotClosed()
        return lua_toboolean(L, index) != 0
    }

    override fun toObject(index: Int): Any? {
        checkNotClosed()
        return when (type(index)) {
            Lua.LuaType.NIL, Lua.LuaType.NONE -> null
            Lua.LuaType.BOOLEAN -> toBoolean(index)
            Lua.LuaType.NUMBER -> {
                if (isInteger(index)) toInteger(index) else toNumber(index)
            }
            Lua.LuaType.STRING -> toString(index)
            Lua.LuaType.TABLE -> toMap(index)
            Lua.LuaType.FUNCTION -> null // TODO: wrap function
            Lua.LuaType.USERDATA -> toJavaObject(index)
            else -> null
        }
    }

    override fun toObject(index: Int, type: KClass<*>?): Any? {
        checkNotClosed()
        if (type == null) return toObject(index)

        return when {
            type == Boolean::class -> toBoolean(index)
            type == Int::class -> toInteger(index).toInt()
            type == Long::class -> toInteger(index)
            type == Double::class -> toNumber(index)
            type == Float::class -> toNumber(index).toFloat()
            type == String::class -> toString(index)
            type == ByteArray::class -> toBuffer(index)
            type == Map::class -> toMap(index)
            type == List::class -> toList(index)
            else -> toObject(index)
        }
    }

    override fun toString(index: Int): String? {
        checkNotClosed()
        memScoped {
            val len = alloc<size_tVar>()
            val str = lua_tolstring(L, index, len.ptr)
            return str?.toKString()
        }
    }

    override fun toBuffer(index: Int): ByteArray? {
        checkNotClosed()
        memScoped {
            val len = alloc<size_tVar>()
            val str = lua_tolstring(L, index, len.ptr)
            if (str == null) return null

            val length = len.value.toInt()
            return ByteArray(length) { i ->
                str[i]
            }
        }
    }

    override fun toDirectBuffer(index: Int): ByteArray? {
        checkNotClosed()
        // In native, we can't create a direct buffer, so just return a copy
        return toBuffer(index)
    }

    override fun toJavaObject(index: Int): Any? {
        checkNotClosed()
        if (!isUserdata(index)) return null

        // TODO: Implement userdata extraction
        throw UnsupportedOperationException("Java object extraction not yet implemented")
    }

    override fun toMap(index: Int): MutableMap<*, *>? {
        checkNotClosed()
        if (!isTable(index)) return null

        val map = mutableMapOf<Any?, Any?>()
        pushNil()
        while (next(index) != 0) {
            val key = toObject(-2)
            val value = toObject(-1)
            map[key] = value
            pop(1)
        }
        return map
    }

    override fun toList(index: Int): MutableList<*>? {
        checkNotClosed()
        if (!isTable(index)) return null

        val list = mutableListOf<Any?>()
        val len = rawLength(index)
        for (i in 1..len) {
            rawGetI(index, i)
            list.add(toObject(-1))
            pop(1)
        }
        return list
    }

    override fun isBoolean(index: Int): Boolean {
        checkNotClosed()
        return lua_type(L, index) == LUA_TBOOLEAN
    }

    override fun isFunction(index: Int): Boolean {
        checkNotClosed()
        return lua_iscfunction(L, index) != 0
    }

    override fun isJavaObject(index: Int): Boolean {
        checkNotClosed()
        return isUserdata(index)
    }

    override fun isNil(index: Int): Boolean {
        checkNotClosed()
        return lua_type(L, index) == LUA_TNIL
    }

    override fun isNone(index: Int): Boolean {
        checkNotClosed()
        return lua_type(L, index) == LUA_TNONE
    }

    override fun isNoneOrNil(index: Int): Boolean {
        checkNotClosed()
        return lua_type(L, index) <= 0
    }

    override fun isNumber(index: Int): Boolean {
        checkNotClosed()
        return lua_isnumber(L, index) != 0
    }

    override fun isInteger(index: Int): Boolean {
        checkNotClosed()
        return lua_isinteger(L, index) != 0
    }

    override fun isString(index: Int): Boolean {
        checkNotClosed()
        return lua_isstring(L, index) != 0
    }

    override fun isTable(index: Int): Boolean {
        checkNotClosed()
        return lua_type(L, index) == LUA_TTABLE
    }

    override fun isThread(index: Int): Boolean {
        checkNotClosed()
        return lua_type(L, index) == LUA_TTHREAD
    }

    override fun isUserdata(index: Int): Boolean {
        checkNotClosed()
        return lua_isuserdata(L, index) != 0
    }

    override fun type(index: Int): Lua.LuaType? {
        checkNotClosed()
        return when (lua_type(L, index)) {
            LUA_TNIL -> Lua.LuaType.NIL
            LUA_TBOOLEAN -> Lua.LuaType.BOOLEAN
            LUA_TLIGHTUSERDATA -> Lua.LuaType.LIGHTUSERDATA
            LUA_TNUMBER -> Lua.LuaType.NUMBER
            LUA_TSTRING -> Lua.LuaType.STRING
            LUA_TTABLE -> Lua.LuaType.TABLE
            LUA_TFUNCTION -> Lua.LuaType.FUNCTION
            LUA_TUSERDATA -> Lua.LuaType.USERDATA
            LUA_TTHREAD -> Lua.LuaType.THREAD
            LUA_TNONE -> Lua.LuaType.NONE
            else -> null
        }
    }

    override fun equal(i1: Int, i2: Int): Boolean {
        checkNotClosed()
        return lua_compare(L, i1, i2, LUA_OPEQ) != 0
    }

    override fun rawLength(index: Int): Int {
        checkNotClosed()
        return lua_rawlen(L, index).convert()
    }

    override fun lessThan(i1: Int, i2: Int): Boolean {
        checkNotClosed()
        return lua_compare(L, i1, i2, LUA_OPLT) != 0
    }

    override fun rawEqual(i1: Int, i2: Int): Boolean {
        checkNotClosed()
        return lua_rawequal(L, i1, i2) != 0
    }

    override var top: Int
        get() {
            checkNotClosed()
            return lua_gettop(L)
        }
        set(value) {
            checkNotClosed()
            lua_settop(L, value)
        }

    override fun insert(index: Int) {
        checkNotClosed()
        lua_rotate(L, (index), 1)
    }

    override fun pop(n: Int) {
        checkNotClosed()
        lua_settop(L, -(n) - 1)
    }

    override fun pushValue(index: Int) {
        checkNotClosed()
        lua_pushvalue(L, index)
    }

    override fun pushThread() {
        checkNotClosed()
        lua_pushthread(L)
    }

    override fun remove(index: Int) {
        checkNotClosed()
        lua_rotate(L, (index), -1)
        pop(1)
    }

    override fun replace(index: Int) {
        checkNotClosed()
        lua_copy(L, -1, (index))
        pop(1)
    }

    override fun xMove(other: Lua?, n: Int) {
        checkNotClosed()
        if (other == null) throw IllegalArgumentException("Other Lua state is null")
        if (other !is NativeLua) throw IllegalArgumentException("Other Lua state is not a NativeLua instance")

        lua_xmove(L, other.L, n)
    }

    override fun load(script: String?) {
        checkNotClosed()
        if (script == null) throw IllegalArgumentException("Script is null")

        val result = luaL_loadstring(L, script)
        if (result != LUA_OK) {
            val error = toString(-1)
            pop(1)
            throw LuaException(luaErrorFromCode(result), error)
        }
    }

    override fun load(buffer: ByteArray?, name: String?) {
        checkNotClosed()
        if (buffer == null) throw IllegalArgumentException("Buffer is null")
        if (name == null) throw IllegalArgumentException("Name is null")

        val result = luaL_loadbufferx(L, buffer.decodeToString(), buffer.size.convert(), name, null)
        if (result != LUA_OK) {
            val error = toString(-1)
            pop(1)
            throw LuaException(luaErrorFromCode(result), error)
        }
    }

    override fun run(script: String?) {
        checkNotClosed()
        load(script)
        pCall(0, LUA_MULTRET)
    }

    override fun run(buffer: ByteArray?, name: String?) {
        checkNotClosed()
        load(buffer, name)
        pCall(0, LUA_MULTRET)
    }

    override fun dump(): ByteArray? {
        checkNotClosed()
        // TODO: Implement lua_dump
        throw UnsupportedOperationException("dump() not yet implemented")
    }

    override fun pCall(nArgs: Int, nResults: Int) {
        checkNotClosed()
        val result = lua_pcallk(L, nArgs, nResults, 0, 0, null)
        if (result != LUA_OK) {
            val error = toString(-1)
            pop(1)
            throw LuaException(luaErrorFromCode(result), error)
        }
    }

    override fun newThread(): Lua? {
        checkNotClosed()
        val newL = lua_newthread(L)
        // TODO: Create a wrapper for the new thread
        throw UnsupportedOperationException("newThread() not yet implemented")
    }

    override fun resume(nArgs: Int): Boolean {
        checkNotClosed()
        memScoped {
            val nresults = alloc<IntVar>()
            val result = lua_resume(L, null, nArgs, nresults.ptr)

            when (result) {
                LUA_OK -> return false
                LUA_YIELD -> return true
                else -> {
                    val error = toString(-1)
                    pop(1)
                    throw LuaException(luaErrorFromCode(result), error)
                }
            }
        }
    }

    override fun status(): LuaException.LuaError {
        checkNotClosed()
        return luaErrorFromCode(lua_status(L))
    }

    override fun yield(n: Int) {
        checkNotClosed()
        throw UnsupportedOperationException("yield() is not supported in Kotlin Native")
    }

    override fun createTable(nArr: Int, nRec: Int) {
        checkNotClosed()
        lua_createtable(L, nArr, nRec)
    }

    override fun newTable() {
        checkNotClosed()
        lua_createtable(L, 0, 0)
    }

    override fun getField(index: Int, key: String?) {
        checkNotClosed()
        if (key == null) throw IllegalArgumentException("Key is null")
        lua_getfield(L, index, key)
    }

    override fun setField(index: Int, key: String?) {
        checkNotClosed()
        if (key == null) throw IllegalArgumentException("Key is null")
        lua_setfield(L, index, key)
    }

    override fun getTable(index: Int) {
        checkNotClosed()
        lua_gettable(L, index)
    }

    override fun setTable(index: Int) {
        checkNotClosed()
        lua_settable(L, index)
    }

    override fun next(n: Int): Int {
        checkNotClosed()
        return lua_next(L, n)
    }

    override fun rawGet(index: Int) {
        checkNotClosed()
        lua_rawget(L, index)
    }

    override fun rawGetI(index: Int, n: Int) {
        checkNotClosed()
        lua_rawgeti(L, index, n.convert())
    }

    override fun rawSet(index: Int) {
        checkNotClosed()
        lua_rawset(L, index)
    }

    override fun rawSetI(index: Int, n: Int) {
        checkNotClosed()
        lua_rawseti(L, index, n.convert())
    }

    override fun ref(index: Int): Int {
        checkNotClosed()
        return luaL_ref(L, index)
    }

    override fun ref(): Int {
        checkNotClosed()
        return luaL_ref(L, LUA_REGISTRYINDEX)
    }

    override fun refGet(ref: Int) {
        checkNotClosed()
        lua_rawgeti(L, LUA_REGISTRYINDEX, ref.convert())
    }

    override fun unRef(index: Int, ref: Int) {
        checkNotClosed()
        luaL_unref(L, index, ref)
    }

    override fun unref(ref: Int) {
        checkNotClosed()
        luaL_unref(L, LUA_REGISTRYINDEX, ref)
    }

    override fun getGlobal(name: String?) {
        checkNotClosed()
        if (name == null) throw IllegalArgumentException("Name is null")
        lua_getglobal(L, name)
    }

    override fun setGlobal(name: String?) {
        checkNotClosed()
        if (name == null) throw IllegalArgumentException("Name is null")
        lua_setglobal(L, name)
    }

    override fun getMetatable(index: Int): Int {
        checkNotClosed()
        return lua_getmetatable(L, index)
    }

    override fun setMetatable(index: Int) {
        checkNotClosed()
        lua_setmetatable(L, index)
    }

    override fun getMetaField(index: Int, field: String?): Int {
        checkNotClosed()
        if (field == null) throw IllegalArgumentException("Field is null")
        return luaL_getmetafield(L, index, field)
    }

    override fun getRegisteredMetatable(typeName: String?) {
        checkNotClosed()
        if (typeName == null) throw IllegalArgumentException("Type name is null")
        (lua_getfield(L, LUA_REGISTRYINDEX, (typeName)))
    }

    override fun newRegisteredMetatable(typeName: String?): Int {
        checkNotClosed()
        if (typeName == null) throw IllegalArgumentException("Type name is null")
        return luaL_newmetatable(L, typeName)
    }

    override fun openLibraries() {
        checkNotClosed()
        luaL_openselectedlibs(L, 0.inv(), 0)
    }

    override fun openLibrary(name: String?) {
        checkNotClosed()
        if (name == null) throw IllegalArgumentException("Library name is null")

//        val fnToOpen: CPointer<CFunction<Function1<CPointer<lua_State>?, Int>>>? = when (name) {
//            "base" -> staticCFunction(::luaopen_base)
//            "package" -> staticCFunction(::luaopen_package)
//            "coroutine" -> staticCFunction(::luaopen_coroutine)
//            "table" -> staticCFunction(::luaopen_table)
//            "io" -> staticCFunction(::luaopen_io)
//            "os" -> staticCFunction(::luaopen_os)
//            "string" -> staticCFunction(::luaopen_string)
//            "math" -> staticCFunction(::luaopen_math)
//            "utf8" -> staticCFunction(::luaopen_utf8)
//            "debug" -> staticCFunction(::luaopen_debug)
//            else -> throw IllegalArgumentException("Unknown library: $name")
//        }

        // Get the library loading function
        luaL_requiref(L as CValuesRef<lua_State>, name, null, 1)
        pop(1)
    }

    override fun concat(n: Int) {
        checkNotClosed()
        lua_concat(L, n)
    }

    override fun gc() {
        checkNotClosed()
        lua_gc(L, LUA_GCCOLLECT)
    }

    override fun error(message: String?) {
        checkNotClosed()
        push(message ?: "Error")
        lua_error(L)
    }

    override fun createProxy(
        interfaces: Array<KClass<*>?>?,
        degree: Lua.Conversion?
    ): Any? {
        checkNotClosed()
        // TODO: Implement proxy creation
        throw UnsupportedOperationException("createProxy() not yet implemented")
    }

    override val mainState: Lua?
        get() {
            checkNotClosed()
            // TODO: Track main state
            return this
        }

    override val pointer: Long
        get() {
            checkNotClosed()
            return L.rawValue.toLong()
        }

    override val id: Int
        get() {
            checkNotClosed()
            return L.rawValue.toLong().toInt()
        }

    override val javaError: Throwable?
        get() {
            checkNotClosed()
            getGlobal(Lua.GLOBAL_THROWABLE)
            val error = toJavaObject(-1)
            pop(1)
            return error as? Throwable
        }

    override fun error(e: Throwable?): Int {
        checkNotClosed()
        if (e == null) {
            pushNil()
            setGlobal(Lua.GLOBAL_THROWABLE)
            return 0
        } else {
            pushJavaObject(e)
            setGlobal(Lua.GLOBAL_THROWABLE)
            push(e.toString())
            return -1
        }
    }

    override fun close() {
        if (!closed && L != null) {
            lua_close(L)
            closed = true
        }
    }

    override fun get(): LuaValue? {
        checkNotClosed()
        // TODO: Implement LuaValue creation from stack top
        throw UnsupportedOperationException("get() not yet implemented")
    }

    override fun set(key: String?, value: Any?) {
        checkNotClosed()
        if (key == null) throw IllegalArgumentException("Key is null")
        push(value, Lua.Conversion.FULL)
        setGlobal(key)
    }

    override fun get(globalName: String?): LuaValue? {
        checkNotClosed()
        if (globalName == null) throw IllegalArgumentException("Global name is null")
        getGlobal(globalName)
        return get()
    }

    override fun register(name: String?, function: LuaFunction?) {
        checkNotClosed()
        if (name == null) throw IllegalArgumentException("Name is null")
        if (function == null) throw IllegalArgumentException("Function is null")

        push(function)
        setGlobal(name)
    }

    override fun eval(command: String?): Array<LuaValue?>? {
        checkNotClosed()
        if (command == null) throw IllegalArgumentException("Command is null")

        val oldTop = top
        run(command)
        val newTop = top
        val results = Array<LuaValue?>(newTop - oldTop) { null }

        for (i in results.indices) {
            results[i] = get()
        }

        return results
    }

    override fun require(module: String?): LuaValue? {
        checkNotClosed()
        if (module == null) throw IllegalArgumentException("Module is null")

        getGlobal("require")
        push(module)
        pCall(1, 1)
        return get()
    }

    override fun fromNull(): LuaValue? {
        checkNotClosed()
        pushNil()
        return get()
    }

    override fun from(b: Boolean): LuaValue? {
        checkNotClosed()
        push(b)
        return get()
    }

    override fun from(n: Double): LuaValue? {
        checkNotClosed()
        push(n)
        return get()
    }

    override fun from(n: Long): LuaValue? {
        checkNotClosed()
        push(n)
        return get()
    }

    override fun from(s: String?): LuaValue? {
        checkNotClosed()
        push(s)
        return get()
    }

    override fun from(buffer: ByteArray?): LuaValue? {
        checkNotClosed()
        push(buffer)
        return get()
    }

    private fun luaErrorFromCode(code: Int): LuaException.LuaError {
        return when (code) {
            LUA_OK -> LuaException.LuaError.OK
            LUA_YIELD -> LuaException.LuaError.YIELD
            LUA_ERRRUN -> LuaException.LuaError.RUNTIME
            LUA_ERRSYNTAX -> LuaException.LuaError.SYNTAX
            LUA_ERRMEM -> LuaException.LuaError.MEMORY
            LUA_ERRERR -> LuaException.LuaError.HANDLER
            LUA_ERRFILE -> LuaException.LuaError.FILE
            else -> LuaException.LuaError.UNKNOWN
        }
    }
}