package io.github.lemcoder.needle.module

import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.asString

class TestLuaLoggingModule(private val engine: ScriptEngine) : LoggingModule {
    var onDebugCalled: ((tag: String, message: String) -> Unit)? = null
    var onInfoCalled: ((tag: String, message: String) -> Unit)? = null
    var onWarnCalled: ((tag: String, message: String) -> Unit)? = null
    var onErrorCalled: ((tag: String, message: String) -> Unit)? = null

    override fun install() {
        engine.registerFunction("__log_d") { args ->
            val tag = args[0].asString()
            val message = args[1].asString()
            d(tag, message)
            ScriptValue.Nil
        }

        engine.registerFunction("__log_i") { args ->
            val tag = args[0].asString()
            val message = args[1].asString()
            i(tag, message)
            ScriptValue.Nil
        }

        engine.registerFunction("__log_w") { args ->
            val tag = args[0].asString()
            val message = args[1].asString()
            w(tag, message)
            ScriptValue.Nil
        }

        engine.registerFunction("__log_e") { args ->
            val tag = args[0].asString()
            val message = args[1].asString()
            e(tag, message)
            ScriptValue.Nil
        }

        engine.eval(
            """
            log = {}
            function log:d(tag, message)
                __log_d(tag, message)
            end
            function log:i(tag, message)
                __log_i(tag, message)
            end
            function log:w(tag, message)
                __log_w(tag, message)
            end
            function log:e(tag, message)
                __log_e(tag, message)
            end
            """.trimIndent()
        )
    }

    override fun d(tag: String, message: String) {
        onDebugCalled?.invoke(tag, message)
    }

    override fun i(tag: String, message: String) {
        onInfoCalled?.invoke(tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        onWarnCalled?.invoke(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        onErrorCalled?.invoke(tag, message)
    }
}
