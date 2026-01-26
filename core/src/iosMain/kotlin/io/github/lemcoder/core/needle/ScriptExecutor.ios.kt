package io.github.lemcoder.core.needle

import io.github.lemcoder.core.needle.converter.ScriptValueConverter
import io.github.lemcoder.core.needle.module.FileSystemModule
import io.github.lemcoder.core.needle.module.LoggingModule
import io.github.lemcoder.core.needle.module.LuaFileSystemModule
import io.github.lemcoder.core.needle.module.LuaLoggingModule
import io.github.lemcoder.core.needle.module.LuaNetworkModule
import io.github.lemcoder.core.needle.module.NetworkModule
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun createScriptExecutor(
    baseDirPath: String,
    engine: ScriptEngine,
    loggingModule: LoggingModule?,
    networkModule: NetworkModule?,
    fileSystemModule: FileSystemModule?
): ScriptExecutor {
    // For iOS, get the documents directory as the base directory
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
    val documentsPath = (urls.firstOrNull() as? NSURL)?.path ?: ""

    val logModule = loggingModule ?: LuaLoggingModule(engine)
    val netModule = networkModule ?: LuaNetworkModule(engine)
    val fsModule = fileSystemModule ?: LuaFileSystemModule(engine, documentsPath)

    return NativeScriptExecutor(engine, logModule, netModule, fsModule)
}

internal class NativeScriptExecutor(
    private val engine: ScriptEngine,
    private val logModule: LoggingModule,
    private val networkModule: NetworkModule,
    private val fileSystemModule: FileSystemModule,
) : ScriptExecutor {

    init {
        // Install modules once at initialization
        logModule.install()
        networkModule.install()
        fileSystemModule.install()
    }

    override fun <OUT> run(code: String, args: Map<String, Any?>): OUT? {
        // Inject args into Lua globals
        args.forEach { (key, value) ->
            engine.setGlobal(
                key,
                value?.let { ScriptValueConverter.toScriptValue(it) } ?: ScriptValue.Nil,
            )
        }

        // Wrap code to always return a value
        val wrappedCode =
            """
            return (function()
                $code
            end)()
            """
                .trimIndent()

        val result = engine.eval(wrappedCode)

        return ScriptValueConverter.toKotlin(result) as? OUT
    }
}