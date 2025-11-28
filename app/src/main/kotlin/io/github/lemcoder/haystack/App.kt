package io.github.lemcoder.haystack

import android.app.Application
import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this@App

        Python.start(AndroidPlatform(instance))
    }

    companion object {
        internal lateinit var instance: App
            private set
        val context: Context by lazy { instance.applicationContext }
    }
}