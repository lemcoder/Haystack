package io.github.lemcoder.core.utils

import android.util.Log

internal actual fun logI(tag: String, message: String) {
    Log.i(tag, message)
}

internal actual fun logD(tag: String, message: String) {
    Log.d(tag, message)
}

internal actual fun logW(tag: String, message: String, throwable: Throwable?) {
    if (throwable != null) {
        Log.w(tag, message, throwable)
    } else {
        Log.w(tag, message)
    }
}

internal actual fun logE(tag: String, message: String, throwable: Throwable?) {
    if (throwable != null) {
        Log.e(tag, message, throwable)
    } else {
        Log.e(tag, message)
    }
}
