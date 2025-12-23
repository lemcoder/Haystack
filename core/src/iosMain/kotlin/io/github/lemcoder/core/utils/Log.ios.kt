package io.github.lemcoder.core.utils

import platform.Foundation.NSLog

internal actual fun logI(tag: String, message: String) {
  NSLog("[INFO] %s: %s", tag, message)
}

internal actual fun logD(tag: String, message: String) {
  NSLog("[DEBUG] %s: %s", tag, message)
}

internal actual fun logW(tag: String, message: String, throwable: Throwable?) {
  if (throwable != null) {
    NSLog("[WARN] %s: %s\n%s", tag, message, throwable.message)
  } else {
    NSLog("[WARN] %s: %s", tag, message)
  }
}

internal actual fun logE(tag: String, message: String, throwable: Throwable?) {
  if (throwable != null) {
    NSLog("[ERROR] %s: %s\n%s", tag, message, throwable.message)
  } else {
    NSLog("[ERROR] %s: %s", tag, message)
  }
}
