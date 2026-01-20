package io.github.lemcoder.needle.module

import android.util.Log
import party.iroiro.luajava.AbstractLua

internal class LuaLoggingModule(private val lua: AbstractLua) : LoggingModule {
    /** Helper object to expose logging functions to Lua */
    private val loggingApi =
        object {
            fun d(tag: String, message: String) = this@LuaLoggingModule.d(tag, message)

            fun i(tag: String, message: String) = this@LuaLoggingModule.i(tag, message)

            fun w(tag: String, message: String) = this@LuaLoggingModule.w(tag, message)

            fun e(tag: String, message: String) = this@LuaLoggingModule.e(tag, message)
        }

    override fun install() =
        with(lua) {
            set("__logging_api", loggingApi)

            run(
                """
                log = {}
                function log:d(tag, message)
                    __logging_api:d(tag, message)
                end
                function log:i(tag, message)
                    __logging_api:i(tag, message)
                end
                function log:w(tag, message)
                    __logging_api:w(tag, message)
                end
                function log:e(tag, message)
                    __logging_api:e(tag, message)
                end
                """
                    .trimIndent()
            )
        }

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}
