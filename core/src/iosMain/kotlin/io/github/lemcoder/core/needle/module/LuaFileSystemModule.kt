package io.github.lemcoder.core.needle.module

import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.asString
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
internal class LuaFileSystemModule(private val engine: ScriptEngine, private val baseDir: String) :
    FileSystemModule {

    private val fileManager = NSFileManager.defaultManager

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
            """
                .trimIndent()
        )
    }

    override fun read(path: String): String? =
        try {
            val fullPath = resolvePath(path)
            if (!fileManager.fileExistsAtPath(fullPath)) {
                null
            } else {
                NSString.stringWithContentsOfFile(
                    fullPath,
                    encoding = NSUTF8StringEncoding,
                    error = null,
                )
            }
        } catch (e: Exception) {
            null
        }

    override fun write(path: String, content: String): Boolean =
        try {
            val fullPath = resolvePath(path)
            val parentPath = (fullPath as NSString).stringByDeletingLastPathComponent

            // Create parent directories if needed
            if (!fileManager.fileExistsAtPath(parentPath)) {
                fileManager.createDirectoryAtPath(
                    parentPath,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null,
                )
            }

            (content as NSString).writeToFile(
                fullPath,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = null,
            )
        } catch (e: Exception) {
            false
        }

    override fun delete(path: String): Boolean =
        try {
            val fullPath = resolvePath(path)
            fileManager.fileExistsAtPath(fullPath) &&
                fileManager.removeItemAtPath(fullPath, error = null)
        } catch (e: Exception) {
            false
        }

    override fun exists(path: String): Boolean =
        try {
            val fullPath = resolvePath(path)
            fileManager.fileExistsAtPath(fullPath)
        } catch (e: Exception) {
            false
        }

    override fun list(path: String): List<String> =
        try {
            val fullPath = resolvePath(path)
            val contents = fileManager.contentsOfDirectoryAtPath(fullPath, error = null)
            contents?.mapNotNull { it as? String } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

    private fun resolvePath(path: String): String {
        // Resolve path relative to baseDir
        return if (path.startsWith("/")) {
            path
        } else {
            "$baseDir/$path"
        }
    }
}
