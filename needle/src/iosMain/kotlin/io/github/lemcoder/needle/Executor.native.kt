package io.github.lemcoder.needle

import io.github.lemcoder.needle.converter.ScriptValueConverter
import io.github.lemcoder.needle.module.FileSystemModule
import io.github.lemcoder.needle.module.LoggingModule
import io.github.lemcoder.needle.module.LuaFileSystemModule
import io.github.lemcoder.needle.module.LuaLoggingModule
import io.github.lemcoder.needle.module.LuaNetworkModule
import io.github.lemcoder.needle.module.NetworkModule
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.instantiateScriptEngine
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun createExecutor(
    context: Any,
    engine: ScriptEngine,
    loggingModule: LoggingModule?,
    networkModule: NetworkModule?,
    fileSystemModule: FileSystemModule?
): Executor {
    // For iOS, get the documents directory as the base directory
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask) as List<*>
    val documentsPath = (urls.firstOrNull() as? platform.Foundation.NSURL)?.path ?: ""

    val logModule = loggingModule ?: LuaLoggingModule(engine)
    val netModule = networkModule ?: LuaNetworkModule(engine)
    val fsModule = fileSystemModule ?: LuaFileSystemModule(engine, documentsPath)

    return NativeExecutor(engine, logModule, netModule, fsModule)
}

internal class NativeExecutor(
    private val engine: ScriptEngine,
    private val logModule: LoggingModule,
    private val networkModule: NetworkModule,
    private val fileSystemModule: FileSystemModule,
) : Executor {

    init {
        // Install modules once at initialization
        logModule.install()
        networkModule.install()
        fileSystemModule.install()
    }

    override fun <OUT> run(code: String, args: Map<String, Any?>): OUT? {
        // Inject args into Lua globals
        args.forEach { (key, value) ->
            engine.setGlobal(key, value?.let { ScriptValueConverter.toScriptValue(it) } ?: ScriptValue.Nil)
        }

        // Wrap code to always return a value
        val wrappedCode =
            """
            return (function()
                $code
            end)()
            """.trimIndent()

        val result = engine.eval(wrappedCode)

        return ScriptValueConverter.toKotlin(result) as? OUT
    }
}