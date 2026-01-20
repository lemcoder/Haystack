package io.github.lemcoder.needle.module

import party.iroiro.luajava.AbstractLua
import party.iroiro.luajava.Lua

internal class TestLuaFileSystemModule(private val lua: AbstractLua) : FileSystemModule {
    // In-memory file system for testing
    private val files = mutableMapOf<String, String>()
    private val directories = mutableSetOf<String>()

    var onReadCalled: ((path: String) -> Unit)? = null
    var onWriteCalled: ((path: String, content: String) -> Unit)? = null
    var onDeleteCalled: ((path: String) -> Unit)? = null
    var onExistsCalled: ((path: String) -> Unit)? = null
    var onListCalled: ((path: String) -> Unit)? = null

    /** Helper object to expose filesystem functions to Lua */
    private val fileSystemApi =
        object {
            fun read(path: String) = this@TestLuaFileSystemModule.read(path)

            fun write(path: String, content: String) =
                this@TestLuaFileSystemModule.write(path, content)

            fun delete(path: String) = this@TestLuaFileSystemModule.delete(path)

            fun exists(path: String) = this@TestLuaFileSystemModule.exists(path)

            fun list(path: String) = this@TestLuaFileSystemModule.list(path)
        }

    override fun install() =
        with(lua) {
            push { lua ->
                val javaList =
                    lua.toJavaObject(1) as? List<*>
                        ?: throw IllegalArgumentException("Expected List object")
                pushList(lua, javaList)
                1
            }
            setGlobal("__convertListToTable")
            set("__filesystem_api", fileSystemApi)

            run(
                """
                fs = {}
                function fs:read(path)
                    return __filesystem_api:read(path)
                end
                function fs:write(path, content)
                    return __filesystem_api:write(path, content)
                end
                function fs:delete(path)
                    return __filesystem_api:delete(path)
                end
                function fs:exists(path)
                    return __filesystem_api:exists(path)
                end
                function fs:list(path)
                    local result = __filesystem_api:list(path)
                    return __convertListToTable(result)
                end
                """
                    .trimIndent()
            )
        }

    override fun read(path: String): String? {
        onReadCalled?.invoke(path)
        return files[path]
    }

    override fun write(path: String, content: String): Boolean {
        onWriteCalled?.invoke(path, content)
        files[path] = content
        // Automatically create parent directory
        val parentPath = path.substringBeforeLast('/', "")
        if (parentPath.isNotEmpty()) {
            directories.add(parentPath)
        }
        return true
    }

    override fun delete(path: String): Boolean {
        onDeleteCalled?.invoke(path)
        return files.remove(path) != null || directories.remove(path)
    }

    override fun exists(path: String): Boolean {
        onExistsCalled?.invoke(path)
        return files.containsKey(path) || directories.contains(path)
    }

    override fun list(path: String): List<String> {
        onListCalled?.invoke(path)
        val pathPrefix = if (path.isEmpty() || path == ".") "" else "$path/"
        val filesInDir =
            files.keys
                .filter {
                    it.startsWith(pathPrefix) &&
                        it.substring(pathPrefix.length).count { c -> c == '/' } == 0
                }
                .map { it.substring(pathPrefix.length) }

        val dirsInDir =
            directories
                .filter {
                    it.startsWith(pathPrefix) &&
                        it != path &&
                        it.substring(pathPrefix.length).count { c -> c == '/' } == 0
                }
                .map { it.substring(pathPrefix.length) }

        return (filesInDir + dirsInDir).sorted()
    }

    // Helper methods for test setup
    fun setupFile(path: String, content: String) {
        files[path] = content
    }

    fun setupDirectory(path: String) {
        directories.add(path)
    }

    fun clear() {
        files.clear()
        directories.clear()
    }

    private fun pushList(lua: Lua, list: List<*>?) {
        lua.createTable(list?.size ?: 0, 0)
        list?.forEachIndexed { index, value ->
            lua.push(index + 1)
            when (value) {
                is String -> lua.push(value)
                is Number -> lua.push(value.toDouble())
                else -> lua.pushNil()
            }
            lua.setTable(-3)
        }
    }
}
