package io.github.lemcoder.core.utils

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

internal actual fun getFilesDirPath(): String {
    return NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first()
        as String
}
