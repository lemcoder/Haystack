package io.github.lemcoder.core.data.samples

import io.github.lemcoder.core.model.needle.Needle

/** Interface for providing sample needles */
interface SampleNeedle {
    fun create(): Needle
}
