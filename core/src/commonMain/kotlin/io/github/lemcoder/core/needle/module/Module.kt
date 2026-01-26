package io.github.lemcoder.core.needle.module

sealed interface Module {
    val name: String

    fun install()
}
