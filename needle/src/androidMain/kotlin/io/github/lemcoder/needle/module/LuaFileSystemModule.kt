package io.github.lemcoder.needle.module

import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.asString
import java.io.File

internal class LuaFileSystemModule(
    private val engine: ScriptEngine,
    private val baseDir: File
) : FileSystemModule {

    override fun install() {
        engine.registerFunction("__fs_read") { args ->
            val path = args[0].asString()
            read(path)?.let { ScriptValue.Str(it) } ?: ScriptValue.Nil
        }

        engine.registerFunction("__fs_write") { args ->
            val path = args[0].asString()
            val content = args[1].asString()
            ScriptValue.Bool(write(path, content))
        }

        engine.registerFunction("__fs_delete") { args ->
            val path = args[0].asString()
            ScriptValue.Bool(delete(path))
        }

        engine.registerFunction("__fs_exists") { args ->
            val path = args[0].asString()
            ScriptValue.Bool(exists(path))
        }

        engine.registerFunction("__fs_list") { args ->
            val path = args[0].asString()
            val list = list(path)
            ScriptValue.ListVal(list.map { ScriptValue.Str(it) })
        }

        engine.eval(
            """
            fs = {}
            function fs:read(path)
                return __fs_read(path)
            end
            function fs:write(path, content)
                return __fs_write(path, content)
            end
            function fs:delete(path)
                return __fs_delete(path)
            end
            function fs:exists(path)
                return __fs_exists(path)
            end
            function fs:list(path)
                return __fs_list(path)
            end
            """.trimIndent()
        )
    }

    override fun read(path: String): String? =
        try {
            val file = File(baseDir, path)
            if (!file.exists() || !file.isFile) null
            else file.readText()
        } catch (e: Exception) {
            null
        }

    override fun write(path: String, content: String): Boolean =
        try {
            val file = File(baseDir, path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }

    override fun delete(path: String): Boolean =
        try {
            val file = File(baseDir, path)
            file.exists() && file.delete()
        } catch (e: Exception) {
            false
        }

    override fun exists(path: String): Boolean =
        try {
            File(baseDir, path).exists()
        } catch (e: Exception) {
            false
        }

    override fun list(path: String): List<String> =
        try {
            val dir = File(baseDir, path)
            if (!dir.exists() || !dir.isDirectory) emptyList()
            else dir.listFiles()?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
}
