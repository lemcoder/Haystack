package io.github.lemcoder.needle

import android.content.Context
import io.github.lemcoder.needle.converter.ScriptValueConverter
import io.github.lemcoder.needle.module.FileSystemModule
import io.github.lemcoder.needle.module.LoggingModule
import io.github.lemcoder.needle.module.LuaFileSystemModule
import io.github.lemcoder.needle.module.LuaLoggingModule
import io.github.lemcoder.needle.module.LuaNetworkModule
import io.github.lemcoder.needle.module.NetworkModule
import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue

actual fun createExecutor(
    context: Any,
    engine: ScriptEngine,
    loggingModule: LoggingModule?,
    networkModule: NetworkModule?,
    fileSystemModule: FileSystemModule?,
): Executor {
    context as Context

    val logModule = loggingModule ?: LuaLoggingModule(engine)
    val networkModule = networkModule ?: LuaNetworkModule(engine)
    val fileSystemModule = fileSystemModule ?: LuaFileSystemModule(engine, context.filesDir)

    return AndroidExecutor(engine, logModule, networkModule, fileSystemModule)
}

internal class AndroidExecutor(
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
