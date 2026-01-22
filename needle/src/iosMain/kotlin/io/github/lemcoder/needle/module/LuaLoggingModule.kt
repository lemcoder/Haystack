package io.github.lemcoder.needle.module

import io.github.lemcoder.scriptEngine.ScriptEngine
import io.github.lemcoder.scriptEngine.ScriptValue
import io.github.lemcoder.scriptEngine.asString
import platform.Foundation.NSLog

internal class LuaLoggingModule(private val engine: ScriptEngine) : LoggingModule {

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
        NSLog("[DEBUG] %@: %@", tag, message)
    }

    override fun i(tag: String, message: String) {
        NSLog("[INFO] %@: %@", tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("[WARN] %@: %@ - %@", tag, message, throwable.message ?: throwable.toString())
        } else {
            NSLog("[WARN] %@: %@", tag, message)
        }
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("[ERROR] %@: %@ - %@", tag, message, throwable.message ?: throwable.toString())
        } else {
            NSLog("[ERROR] %@: %@", tag, message)
        }
    }
}
