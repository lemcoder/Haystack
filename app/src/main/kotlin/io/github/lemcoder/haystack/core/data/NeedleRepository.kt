package io.github.lemcoder.haystack.core.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.lemcoder.haystack.App
import io.github.lemcoder.haystack.core.model.needle.Needle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.needlesDataStore: DataStore<Preferences> by preferencesDataStore(name = "needles")

interface NeedleRepository {
    val needlesFlow: Flow<List<Needle>>
    suspend fun getAllNeedles(): List<Needle>
    suspend fun getNeedleById(id: String): Needle?
    suspend fun saveNeedle(needle: Needle)
    suspend fun updateNeedle(needle: Needle)
    suspend fun deleteNeedle(id: String)
    suspend fun deleteAllNeedles()

    companion object {
        val Instance: NeedleRepository by lazy {
            NeedleRepositoryImpl()
        }
    }
}

class NeedleRepositoryImpl(
    private val context: Context = App.context
) : NeedleRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override val needlesFlow: Flow<List<Needle>> =
        context.needlesDataStore.data.map { preferences ->
            val needlesJson = preferences[NEEDLES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<Needle>>(needlesJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing needles", e)
                emptyList()
            }
        }

    override suspend fun getAllNeedles(): List<Needle> {
        return needlesFlow.first()
    }

    override suspend fun getNeedleById(id: String): Needle? {
        return getAllNeedles().find { it.id == id }
    }

    override suspend fun saveNeedle(needle: Needle) {
        context.needlesDataStore.edit { preferences ->
            val currentNeedles = getAllNeedles().toMutableList()

            // Check if needle with this ID already exists
            val existingIndex = currentNeedles.indexOfFirst { it.id == needle.id }
            if (existingIndex != -1) {
                // Update existing needle
                currentNeedles[existingIndex] = needle.copy(updatedAt = System.currentTimeMillis())
            } else {
                // Add new needle
                currentNeedles.add(needle)
            }

            val needlesJson = json.encodeToString(currentNeedles)
            preferences[NEEDLES_KEY] = needlesJson
        }
    }

    override suspend fun updateNeedle(needle: Needle) {
        context.needlesDataStore.edit { preferences ->
            val currentNeedles = getAllNeedles().toMutableList()
            val index = currentNeedles.indexOfFirst { it.id == needle.id }

            if (index != -1) {
                currentNeedles[index] = needle.copy(updatedAt = System.currentTimeMillis())
                val needlesJson = json.encodeToString(currentNeedles)
                preferences[NEEDLES_KEY] = needlesJson
            } else {
                Log.w(TAG, "Attempted to update non-existent needle: ${needle.id}")
            }
        }
    }

    override suspend fun deleteNeedle(id: String) {
        context.needlesDataStore.edit { preferences ->
            val currentNeedles = getAllNeedles().toMutableList()
            currentNeedles.removeAll { it.id == id }

            val needlesJson = json.encodeToString(currentNeedles)
            preferences[NEEDLES_KEY] = needlesJson
        }
    }

    override suspend fun deleteAllNeedles() {
        context.needlesDataStore.edit { preferences ->
            preferences.remove(NEEDLES_KEY)
        }
    }

    companion object {
        private const val TAG = "NeedleRepository"
        private val NEEDLES_KEY = stringPreferencesKey("needles")
    }
}
