package io.github.lemcoder.needle.lua.module

interface NetworkModule: Module {
    override val name: String
        get() = "network"

    override fun install() { }

    fun get(url: String): Map<String, Any?>

    fun post(url: String, body: String): Map<String, Any?>
}