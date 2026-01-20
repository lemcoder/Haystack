package io.github.lemcoder.needle.module

interface FileSystemModule: Module {
    override val name: String
        get() = "filesystem"

    override fun install() { }

    fun read(path: String): String?

    fun write(path: String, content: String): Boolean

    fun delete(path: String): Boolean

    fun exists(path: String): Boolean

    fun list(path: String): List<String>
}
