package io.github.lemcoder.needle.module

import io.github.lemcoder.lua.Lua
import io.github.lemcoder.needle.util.convertListToTable
import io.github.lemcoder.needle.util.pushList
import java.io.File

internal class LuaFileSystemModule(private val lua: Lua, private val baseDir: File) :
    FileSystemModule {
    /** Helper object to expose filesystem functions to Lua */
    private val fileSystemApi =
        object {
            fun read(path: String) = this@LuaFileSystemModule.read(path)

            fun write(path: String, content: String) = this@LuaFileSystemModule.write(path, content)

            fun delete(path: String) = this@LuaFileSystemModule.delete(path)

            fun exists(path: String) = this@LuaFileSystemModule.exists(path)

            fun list(path: String) = this@LuaFileSystemModule.list(path)
        }

    override fun install() =
        with(lua) {
            register("__convertListToTable", convertListToTable)
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
        return try {
            val file = File(baseDir, path)
            if (!file.exists() || !file.isFile) {
                null
            } else {
                file.readText()
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun write(path: String, content: String): Boolean {
        return try {
            val file = File(baseDir, path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun delete(path: String): Boolean {
        return try {
            val file = File(baseDir, path)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun exists(path: String): Boolean {
        return try {
            File(baseDir, path).exists()
        } catch (e: Exception) {
            false
        }
    }

    override fun list(path: String): List<String> {
        return try {
            val dir = File(baseDir, path)
            if (!dir.exists() || !dir.isDirectory) {
                emptyList()
            } else {
                dir.listFiles()?.map { it.name } ?: emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
