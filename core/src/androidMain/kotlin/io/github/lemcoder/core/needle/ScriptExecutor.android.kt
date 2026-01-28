package io.github.lemcoder.core.needle

import android.content.Context
import io.github.lemcoder.core.needle.converter.ScriptValueConverter
import io.github.lemcoder.core.needle.module.FileSystemModule
import io.github.lemcoder.core.needle.module.LoggingModule
import io.github.lemcoder.core.needle.module.LuaFileSystemModule
import io.github.lemcoder.core.needle.module.LuaLoggingModule
import io.github.lemcoder.core.needle.module.LuaNetworkModule
import io.github.lemcoder.core.needle.module.NetworkModule
import io.github.lemcoder.core.utils.ApplicationContext
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import java.io.File

actual fun createScriptExecutor(
    baseDirPath: String,
    engine: ScriptEngine,
    loggingModule: LoggingModule?,
    networkModule: NetworkModule?,
    fileSystemModule: FileSystemModule?,
): ScriptExecutor {
    val logModule = loggingModule ?: LuaLoggingModule(engine)
    val networkModule = networkModule ?: LuaNetworkModule(engine)
    val fileSystemModule = fileSystemModule ?: LuaFileSystemModule(engine, File(baseDirPath))

    return AndroidScriptExecutor(engine, logModule, networkModule, fileSystemModule)
}

internal class AndroidScriptExecutor(
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

actual fun getBaseDirPath(): String {
    return (ApplicationContext as Context).filesDir.absolutePath
}
