package io.github.lemcoder.haystack

import android.app.Application
import android.content.Context
import io.github.lemcoder.core.utils.HaystackContextProvider

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this@App

        HaystackContextProvider.initialize(instance)
    }

    companion object {
        internal lateinit var instance: App
            private set

        val context: Context by lazy { instance.applicationContext }
    }
}
