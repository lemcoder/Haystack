package io.github.lemcoder.haystack

import android.app.Application
import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import io.github.lemcoder.core.utils.HaystackContextProvider

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this@App

        HaystackContextProvider.initialize(instance)
        Python.start(AndroidPlatform(instance))
    }

    companion object {
        internal lateinit var instance: App
            private set

        val context: Context by lazy { instance.applicationContext }
    }
}
