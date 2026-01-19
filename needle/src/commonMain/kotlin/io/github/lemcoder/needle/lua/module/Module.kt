package io.github.lemcoder.needle.lua.module

sealed interface Module {
    val name: String
    fun install()
}