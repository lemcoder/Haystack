package io.github.lemcoder.core.data.repository

import io.github.lemcoder.core.data.source.DebugNeedleDataSource
import io.github.lemcoder.core.data.source.NeedleDataSource
import io.github.lemcoder.core.data.store.DataStoreNeedleStore
import io.github.lemcoder.core.data.store.NeedleStore
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.utils.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface NeedleRepository {
    val needlesFlow: Flow<List<Needle>>
    val visibleNeedlesFlow: Flow<List<Needle>>
    val hiddenNeedleIdsFlow: Flow<Set<String>>

    suspend fun getAllNeedles(): List<Needle>

    suspend fun getVisibleNeedles(): List<Needle>

    suspend fun getNeedleById(id: String): Needle?

    suspend fun isNeedleHidden(id: String): Boolean

    suspend fun hideNeedle(id: String)

    suspend fun showNeedle(id: String)

    suspend fun toggleNeedleVisibility(id: String)

    suspend fun saveNeedle(needle: Needle)

    suspend fun updateNeedle(needle: Needle)

    suspend fun deleteNeedle(id: String)

    suspend fun deleteAllNeedles()

    companion object {
        val Instance: NeedleRepository by lazy { NeedleRepositoryImpl() }
    }
}

/**
 * Repository layer for managing needles. This layer provides business logic on top of the storage
 * layer (NeedleStore), including:
 * - Initialization with sample needles
 * - Visibility filtering
 * - Convenience methods for common operations
 *
 * The actual CRUD operations are delegated to NeedleStore, which can be swapped (e.g., from
 * DataStore to Room) without changing this repository.
 */
class NeedleRepositoryImpl(
    private val store: NeedleStore = DataStoreNeedleStore(),
    private val debugDataSource: NeedleDataSource = DebugNeedleDataSource(),
) : NeedleRepository {

    private var isInitialized = false
    private val initMutex = Mutex()

    /**
     * Initializes the repository with sample needles if the store is empty. This should be called
     * once when the repository is first accessed.
     */
    private suspend fun initializeIfNeeded() {
        if (isInitialized) return

        initMutex.withLock {
            if (isInitialized) return

            try {
                val currentNeedles = store.getAllNeedles()
                if (currentNeedles.isEmpty()) {
                    Log.d(TAG, "Store is empty, initializing with sample needles")
                    val sampleNeedles = debugDataSource.getNeedles()
                    sampleNeedles.forEach { needle -> store.saveNeedle(needle) }
                    Log.d(TAG, "Initialized ${sampleNeedles.size} sample needles")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing sample needles", e)
            } finally {
                isInitialized = true
            }
        }
    }

    override val needlesFlow: Flow<List<Needle>> =
        store.needlesFlow.onStart { initializeIfNeeded() }

    override val hiddenNeedleIdsFlow: Flow<Set<String>> = store.hiddenNeedleIdsFlow

    override val visibleNeedlesFlow: Flow<List<Needle>> =
        store.needlesFlow.map { needles ->
            val hiddenIds = store.getHiddenNeedleIds()
            needles.filter { it.id !in hiddenIds }
        }

    override suspend fun getAllNeedles(): List<Needle> {
        return store.getAllNeedles()
    }

    override suspend fun getVisibleNeedles(): List<Needle> {
        return visibleNeedlesFlow.first()
    }

    override suspend fun getNeedleById(id: String): Needle? {
        return store.getAllNeedles().find { it.id == id }
    }

    override suspend fun isNeedleHidden(id: String): Boolean {
        return id in store.getHiddenNeedleIds()
    }

    override suspend fun hideNeedle(id: String) {
        store.hideNeedle(id)
    }

    override suspend fun showNeedle(id: String) {
        store.showNeedle(id)
    }

    override suspend fun toggleNeedleVisibility(id: String) {
        if (isNeedleHidden(id)) {
            showNeedle(id)
        } else {
            hideNeedle(id)
        }
    }

    override suspend fun saveNeedle(needle: Needle) {
        store.saveNeedle(needle)
    }

    override suspend fun updateNeedle(needle: Needle) {
        store.updateNeedle(needle)
    }

    override suspend fun deleteNeedle(id: String) {
        store.deleteNeedle(id)
    }

    override suspend fun deleteAllNeedles() {
        store.deleteAllNeedles()
    }

    companion object {
        private const val TAG = "NeedleRepository"
    }
}
