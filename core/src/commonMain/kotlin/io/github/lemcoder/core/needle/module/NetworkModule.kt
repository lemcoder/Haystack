package io.github.lemcoder.core.needle.module

interface NetworkModule : Module {
    override val name: String
        get() = "network"

    override fun install() {}

    fun get(url: String): Map<String, Any?>

    fun post(url: String, body: String): Map<String, Any?>
}
