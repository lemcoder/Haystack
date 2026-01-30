package io.github.lemcoder.core.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.utils.Log
import io.github.lemcoder.core.utils.createDataStore
import io.github.lemcoder.core.utils.currentTimeMillis
import io.github.lemcoder.core.utils.getFilesDirPath
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * DataStore-based implementation of NeedleStore.
 * Uses AndroidX DataStore with Preferences for persisting needles as JSON.
 *
 * This implementation will eventually be replaced with a Room database implementation
 * for better performance and query capabilities.
 */
class DataStoreNeedleStore : NeedleStore {

    private val dataStore: DataStore<Preferences> by lazy {
        createDataStore { getFilesDirPath() + "needles.preferences_pb" }
    }

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override val needlesFlow: Flow<List<Needle>> =
        dataStore.data.map { preferences ->
            val needlesJson = preferences[NEEDLES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Needle>>(needlesJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing needles", e)
                emptyList()
            }
        }

    override val hiddenNeedleIdsFlow: Flow<Set<String>> =
        dataStore.data.map { preferences ->
            val hiddenIdsJson = preferences[HIDDEN_NEEDLE_IDS_KEY] ?: "[]"
            try {
                json.decodeFromString<Set<String>>(hiddenIdsJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing hidden needle IDs", e)
                emptySet()
            }
        }

    override suspend fun getAllNeedles(): List<Needle> {
        return needlesFlow.first()
    }

    override suspend fun getHiddenNeedleIds(): Set<String> {
        return hiddenNeedleIdsFlow.first()
    }

    override suspend fun saveNeedle(needle: Needle) {
        dataStore.edit { preferences ->
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
        dataStore.edit { preferences ->
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
        dataStore.edit { preferences ->
            val currentNeedles = getAllNeedles().toMutableList()
            currentNeedles.removeAll { it.id == id }

            val needlesJson = json.encodeToString(currentNeedles)
            preferences[NEEDLES_KEY] = needlesJson
        }
    }

    override suspend fun deleteAllNeedles() {
        dataStore.edit { preferences -> preferences.remove(NEEDLES_KEY) }
    }

    override suspend fun hideNeedle(id: String) {
        dataStore.edit { preferences ->
            val hiddenIds = getHiddenNeedleIds().toMutableSet()
            hiddenIds.add(id)
            val hiddenIdsJson = json.encodeToString(hiddenIds)
            preferences[HIDDEN_NEEDLE_IDS_KEY] = hiddenIdsJson
        }
    }

    override suspend fun showNeedle(id: String) {
        dataStore.edit { preferences ->
            val hiddenIds = getHiddenNeedleIds().toMutableSet()
            hiddenIds.remove(id)
            val hiddenIdsJson = json.encodeToString(hiddenIds)
            preferences[HIDDEN_NEEDLE_IDS_KEY] = hiddenIdsJson
        }
    }

    companion object {
        private const val TAG = "DataStoreNeedleStore"
        private val NEEDLES_KEY = stringPreferencesKey("needles")
        private val HIDDEN_NEEDLE_IDS_KEY = stringPreferencesKey("hidden_needle_ids")
    }
}
