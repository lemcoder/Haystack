package io.github.lemcoder.needle.util

import io.github.lemcoder.lua.Lua
import io.github.lemcoder.lua.value.LuaFunction
import io.github.lemcoder.lua.value.LuaValue


internal fun Lua.pushMap(map: Map<*, *>?) {
    createTable(0, map?.size ?: 0)
    map?.forEach { (k, v) ->
        push(k.toString())
        when (v) {
            is Map<*, *> -> pushMap(v)
            is String -> push(v)
            is Number -> push(v.toDouble())
            else -> pushNil()
        }
        setTable(-3)
    }
}

internal fun Lua.pushList(list: List<*>?) {
    createTable(list?.size ?: 0, 0)
    list?.forEachIndexed { index, value ->
        push(index + 1)
        when (value) {
            is Map<*, *> -> pushMap(value)
            is List<*> -> pushList(value)
            is String -> push(value)
            is Number -> push(value.toDouble())
            else -> pushNil()
        }
        setTable(-3)
    }
}

internal val convertMapToTable: LuaFunction = LuaFunction { lua, args ->
    checkNotNull(lua)
    val obj = args?.getOrNull(0)?.toJavaObject()
    val javaMap = obj as? Map<*, *>
        ?: throw IllegalArgumentException("Expected Map object, got ${obj?.javaClass}")

    lua.pushMap(javaMap)
    listOf(lua.get())
}

val convertListToTable = LuaFunction { lua, args ->
    checkNotNull(lua)
    val obj = args?.getOrNull(0)?.toJavaObject()
    val javaList = obj as? List<*>
        ?: throw IllegalArgumentException("Expected List object, got ${obj?.javaClass}")

    lua.pushList(javaList)
    listOf(lua.get())
}
