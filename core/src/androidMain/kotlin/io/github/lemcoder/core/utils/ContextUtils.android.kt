package io.github.lemcoder.core.utils

import android.annotation.SuppressLint

internal typealias AndroidContext = android.content.Context

internal actual val ApplicationContext: Context
    get() = HaystackContextProvider.getContext()

// TODO (mikolaj) replace with context provider library
@SuppressLint("StaticFieldLeak")
object HaystackContextProvider {
    private var initialized = false
    private var context : AndroidContext? = null

    @Synchronized
    fun initialize(context: AndroidContext) {
        this.context = context.applicationContext
        initialized = true
    }

    @Synchronized
    fun getContext(): Context {
        if (!initialized) {
            throw IllegalStateException("ContextProvider is not initialized. Call HaystackContextProvider.initialize() first.")
        }
        return this.context as Context
    }
}