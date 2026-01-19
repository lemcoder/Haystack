package io.github.lemcoder.needle.util

import party.iroiro.luajava.Lua

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
