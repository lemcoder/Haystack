package io.github.lemcoder.core.utils

object Log {
  fun i(tag: String, message: String) = logI(tag, message)

  fun d(tag: String, message: String) = logD(tag, message)

  fun w(tag: String, message: String, throwable: Throwable? = null) = logW(tag, message, throwable)

  fun e(tag: String, message: String, throwable: Throwable? = null) = logE(tag, message, throwable)
}

internal expect fun logI(tag: String, message: String)

internal expect fun logD(tag: String, message: String)

internal expect fun logW(tag: String, message: String, throwable: Throwable? = null)

internal expect fun logE(tag: String, message: String, throwable: Throwable? = null)
