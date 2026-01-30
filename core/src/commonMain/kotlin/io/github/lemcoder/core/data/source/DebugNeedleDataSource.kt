package io.github.lemcoder.core.data.source

import io.github.lemcoder.core.data.sample.SampleNeedles
import io.github.lemcoder.core.model.needle.Needle

/**
 * Debug implementation of NeedleDataSource that provides sample needles for demonstration and
 * testing purposes. This should be used when the app is launched for the first time or in debug
 * builds to showcase the needle functionality.
 */
class DebugNeedleDataSource : NeedleDataSource {
    /**
     * Returns the predefined sample needles including weather API, greeting, and calculator
     * needles.
     */
    override suspend fun getNeedles(): List<Needle> {
        return SampleNeedles.getAll()
    }
}
