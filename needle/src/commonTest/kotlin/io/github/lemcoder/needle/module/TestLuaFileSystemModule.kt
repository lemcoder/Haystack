package io.github.lemcoder.needle.module

import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.asString


internal class TestLuaFileSystemModule(private val engine: ScriptEngine) : FileSystemModule {
    // In-memory file system for testing
    private val files = mutableMapOf<String, String>()
    private val directories = mutableSetOf<String>()

    var onReadCalled: ((path: String) -> Unit)? = null
    var onWriteCalled: ((path: String, content: String) -> Unit)? = null
    var onDeleteCalled: ((path: String) -> Unit)? = null
    var onExistsCalled: ((path: String) -> Unit)? = null
    var onListCalled: ((path: String) -> Unit)? = null

    override fun install() {
        engine.registerFunction("__fs_read") { args ->
            val path = args[0].asString()
            val result = read(path)
            toScriptValue(result)
        }

        engine.registerFunction("__fs_write") { args ->
            val path = args[0].asString()
            val content = args[1].asString()
            val result = write(path, content)
            ScriptValue.Bool(result)
        }

        engine.registerFunction("__fs_delete") { args ->
            val path = args[0].asString()
            val result = delete(path)
            ScriptValue.Bool(result)
        }

        engine.registerFunction("__fs_exists") { args ->
            val path = args[0].asString()
            val result = exists(path)
            ScriptValue.Bool(result)
        }

        engine.registerFunction("__fs_list") { args ->
            val path = args[0].asString()
            val result = list(path)
            ScriptValue.ListVal(result.map { ScriptValue.Str(it) })
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

    private fun toScriptValue(value: Any?): ScriptValue =
        when (value) {
            null -> ScriptValue.Nil
            is String -> ScriptValue.Str(value)
            is Number -> ScriptValue.Num(value.toDouble())
            is Boolean -> ScriptValue.Bool(value)
            is Map<*, *> ->
                ScriptValue.MapVal(
                    value.entries
                        .filter { it.key is String }
                        .associate { (k, v) ->
                            k as String to toScriptValue(v)
                        }
                )
            is List<*> ->
                ScriptValue.ListVal(value.map { toScriptValue(it) })
            else -> ScriptValue.Str(value.toString())
        }
}
