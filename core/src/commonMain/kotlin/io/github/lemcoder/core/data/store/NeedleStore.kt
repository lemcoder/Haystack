package io.github.lemcoder.core.data.store

import io.github.lemcoder.core.model.needle.Needle
import kotlinx.coroutines.flow.Flow

/**
 * Storage abstraction for needle persistence. This interface defines the contract for storing and
 * retrieving needles, allowing different implementations (DataStore, Room, etc.).
 *
 * This separation enables:
 * - Easy migration from DataStore to Room in the future
 * - Better testability with mock implementations
 * - Clear separation between repository (business logic) and store (persistence)
 */
interface NeedleStore {
    /** Flow of all needles in the store */
    val needlesFlow: Flow<List<Needle>>

    /** Flow of hidden needle IDs */
    val hiddenNeedleIdsFlow: Flow<Set<String>>

    /** Get all needles from the store */
    suspend fun getAllNeedles(): List<Needle>

    /** Get all hidden needle IDs */
    suspend fun getHiddenNeedleIds(): Set<String>

    /** Save or update a needle in the store */
    suspend fun saveNeedle(needle: Needle)

    /** Update an existing needle */
    suspend fun updateNeedle(needle: Needle)

    /** Delete a needle by ID */
    suspend fun deleteNeedle(id: String)

    /** Delete all needles from the store */
    suspend fun deleteAllNeedles()

    /** Add a needle ID to the hidden set */
    suspend fun hideNeedle(id: String)

    /** Remove a needle ID from the hidden set */
    suspend fun showNeedle(id: String)
}
