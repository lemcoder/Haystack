package io.github.lemcoder.needle.lua.module

interface LoggingModule: Module {
    override val name: String
        get() = "logging"

    override fun install() { }

    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}