package io.github.lemcoder.core.data.source

import io.github.lemcoder.core.model.needle.Needle

/**
 * Data source interface for providing needles. This abstraction allows different implementations
 * for development/debug purposes (with sample data) and production (with persisted data).
 */
interface NeedleDataSource {
    /**
     * Get all needles from this data source
     *
     * @return List of needles
     */
    suspend fun getNeedles(): List<Needle>
}
