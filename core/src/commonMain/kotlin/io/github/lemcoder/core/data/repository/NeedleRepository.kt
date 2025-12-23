package io.github.lemcoder.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.platform.Log
import io.github.lemcoder.core.platform.createDataStore
import io.github.lemcoder.core.utils.currentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Clock

private val needlesDataStore: DataStore<Preferences> by lazy {
    createDataStore { "needles.preferences_pb" }
}

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

class NeedleRepositoryImpl() : NeedleRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override val needlesFlow: Flow<List<Needle>> =
        needlesDataStore.data.map { preferences ->
            val needlesJson = preferences[NEEDLES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Needle>>(needlesJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing needles", e)
                emptyList()
            }
        }

    override val hiddenNeedleIdsFlow: Flow<Set<String>> =
        needlesDataStore.data.map { preferences ->
            val hiddenIdsJson = preferences[HIDDEN_NEEDLE_IDS_KEY] ?: "[]"
            try {
                json.decodeFromString<Set<String>>(hiddenIdsJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing hidden needle IDs", e)
                emptySet()
            }
        }

    override val visibleNeedlesFlow: Flow<List<Needle>> =
        needlesDataStore.data.map { preferences ->
            val needlesJson = preferences[NEEDLES_KEY] ?: "[]"
            val hiddenIdsJson = preferences[HIDDEN_NEEDLE_IDS_KEY] ?: "[]"
            try {
                val allNeedles = json.decodeFromString<List<Needle>>(needlesJson)
                val hiddenIds = json.decodeFromString<Set<String>>(hiddenIdsJson)
                allNeedles.filter { it.id !in hiddenIds }
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing visible needles", e)
                emptyList()
            }
        }

    override suspend fun getAllNeedles(): List<Needle> {
        return needlesFlow.first()
    }

    override suspend fun getVisibleNeedles(): List<Needle> {
        return visibleNeedlesFlow.first()
    }

    override suspend fun getNeedleById(id: String): Needle? {
        return getAllNeedles().find { it.id == id }
    }

    override suspend fun isNeedleHidden(id: String): Boolean {
        val hiddenIds = hiddenNeedleIdsFlow.first()
        return id in hiddenIds
    }

    override suspend fun hideNeedle(id: String) {
        needlesDataStore.edit { preferences ->
            val hiddenIds = hiddenNeedleIdsFlow.first().toMutableSet()
            hiddenIds.add(id)
            val hiddenIdsJson = json.encodeToString(hiddenIds)
            preferences[HIDDEN_NEEDLE_IDS_KEY] = hiddenIdsJson
        }
    }

    override suspend fun showNeedle(id: String) {
        needlesDataStore.edit { preferences ->
            val hiddenIds = hiddenNeedleIdsFlow.first().toMutableSet()
            hiddenIds.remove(id)
            val hiddenIdsJson = json.encodeToString(hiddenIds)
            preferences[HIDDEN_NEEDLE_IDS_KEY] = hiddenIdsJson
        }
    }

    override suspend fun toggleNeedleVisibility(id: String) {
        if (isNeedleHidden(id)) {
            showNeedle(id)
        } else {
            hideNeedle(id)
        }
    }

    override suspend fun saveNeedle(needle: Needle) {
        needlesDataStore.edit { preferences ->
            val currentNeedles = getAllNeedles().toMutableList()

            // Check if needle with this ID already exists
            val existingIndex = currentNeedles.indexOfFirst { it.id == needle.id }
            if (existingIndex != -1) {
                // Update existing needle
                currentNeedles[existingIndex] =
                    needle.copy(updatedAt = Clock.System.currentTimeMillis())
            } else {
                // Add new needle
                currentNeedles.add(needle)
            }

            val needlesJson = json.encodeToString(currentNeedles)
            preferences[NEEDLES_KEY] = needlesJson
        }
    }

    override suspend fun updateNeedle(needle: Needle) {
        needlesDataStore.edit { preferences ->
            val currentNeedles = getAllNeedles().toMutableList()
            val index = currentNeedles.indexOfFirst { it.id == needle.id }

            if (index != -1) {
                currentNeedles[index] = needle.copy(updatedAt = Clock.System.currentTimeMillis())
                val needlesJson = json.encodeToString(currentNeedles)
                preferences[NEEDLES_KEY] = needlesJson
            } else {
                Log.w(TAG, "Attempted to update non-existent needle: ${needle.id}")
            }
        }
    }

    override suspend fun deleteNeedle(id: String) {
        needlesDataStore.edit { preferences ->
            val currentNeedles = getAllNeedles().toMutableList()
            currentNeedles.removeAll { it.id == id }

            val needlesJson = json.encodeToString(currentNeedles)
            preferences[NEEDLES_KEY] = needlesJson
        }
    }

    override suspend fun deleteAllNeedles() {
        needlesDataStore.edit { preferences -> preferences.remove(NEEDLES_KEY) }
    }

    companion object {
        private const val TAG = "NeedleRepository"
        private val NEEDLES_KEY = stringPreferencesKey("needles")
        private val HIDDEN_NEEDLE_IDS_KEY = stringPreferencesKey("hidden_needle_ids")
    }
}
