package io.github.lemcoder.core.utils

import kotlin.time.Clock

fun Clock.System.currentTimeMillis(): Long {
    return this.now().toEpochMilliseconds()
}