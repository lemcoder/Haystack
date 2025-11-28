package io.github.lemcoder.haystack.core.useCase.samples

import io.github.lemcoder.haystack.core.model.needle.Needle

/**
 * Interface for providing sample needles
 */
interface SampleNeedle {
    fun create(): Needle
}
