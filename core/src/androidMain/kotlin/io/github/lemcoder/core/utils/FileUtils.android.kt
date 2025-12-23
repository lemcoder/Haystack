package io.github.lemcoder.core.utils

internal actual fun getFilesDirPath(): String =
    (ApplicationContext as AndroidContext).filesDir.absolutePath
