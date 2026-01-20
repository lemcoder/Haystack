package io.github.lemcoder.needle.module

sealed interface Module {
    val name: String

    fun install()
}
