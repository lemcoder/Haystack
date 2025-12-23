package io.github.lemcoder.core.platform

internal actual fun logI(tag: String, message: String) {
    android.util.Log.i(tag, message)
}
internal actual fun logD(tag: String, message: String) {
    android.util.Log.d(tag, message)
}
internal actual fun logW(tag: String, message: String, throwable: Throwable?) {
    if (throwable != null) {
        android.util.Log.w(tag, message, throwable)
    } else {
        android.util.Log.w(tag, message)
    }
}
internal actual fun logE(tag: String, message: String, throwable: Throwable?) {
    if (throwable != null) {
        android.util.Log.e(tag, message, throwable)
    } else {
        android.util.Log.e(tag, message)
    }
}