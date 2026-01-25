package io.github.lemcoder.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.lemcoder.core.model.llm.ExecutorType
import io.github.lemcoder.core.model.llm.PromptExecutorConfig
import io.github.lemcoder.core.utils.Log
import io.github.lemcoder.core.utils.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val promptExecutorDataStore: DataStore<Preferences> by lazy {
    createDataStore { "prompt_executor.preferences_pb" }
}

interface PromptExecutorRepository {
    val executorConfigsFlow: Flow<List<PromptExecutorConfig>>
    val selectedExecutorFlow: Flow<PromptExecutorConfig?>

    suspend fun getAllExecutorConfigs(): List<PromptExecutorConfig>

    suspend fun getSelectedExecutor(): PromptExecutorConfig?

    suspend fun getExecutorByType(type: ExecutorType): PromptExecutorConfig?

    suspend fun saveExecutor(config: PromptExecutorConfig)

    suspend fun updateExecutor(config: PromptExecutorConfig)

    suspend fun deleteExecutor(type: ExecutorType)

    suspend fun selectExecutor(type: ExecutorType)

    suspend fun deleteAllExecutors()

    companion object {
        val Instance: PromptExecutorRepository by lazy { PromptExecutorRepositoryImpl() }
    }
}

internal class PromptExecutorRepositoryImpl : PromptExecutorRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    override val executorConfigsFlow: Flow<List<PromptExecutorConfig>> =
        promptExecutorDataStore.data.map { preferences ->
            val executorsJson = preferences[EXECUTORS_KEY] ?: "[]"
            try {
                json.decodeFromString<List<PromptExecutorConfig>>(executorsJson)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing executor configs", e)
                emptyList()
            }
        }

    override val selectedExecutorFlow: Flow<PromptExecutorConfig?> =
        promptExecutorDataStore.data.map { preferences ->
            val selectedTypeString = preferences[SELECTED_EXECUTOR_KEY]
            if (selectedTypeString == null) {
                null
            } else {
                try {
                    val selectedType = ExecutorType.valueOf(selectedTypeString)
                    getAllExecutorConfigs().find { it.executorType == selectedType }
                } catch (e: Exception) {
                    Log.e(TAG, "Error getting selected executor", e)
                    null
                }
            }
        }

    override suspend fun getAllExecutorConfigs(): List<PromptExecutorConfig> {
        return executorConfigsFlow.first()
    }

    override suspend fun getSelectedExecutor(): PromptExecutorConfig? {
        return selectedExecutorFlow.first()
    }

    override suspend fun getExecutorByType(type: ExecutorType): PromptExecutorConfig? {
        return getAllExecutorConfigs().find { it.executorType == type }
    }

    override suspend fun saveExecutor(config: PromptExecutorConfig) {
        promptExecutorDataStore.edit { preferences ->
            val currentExecutors = getAllExecutorConfigs().toMutableList()

            // Check if executor with this type already exists
            val existingIndex = currentExecutors.indexOfFirst { it.executorType == config.executorType }
            if (existingIndex != -1) {
                // Update existing executor
                currentExecutors[existingIndex] = config
            } else {
                // Add new executor
                currentExecutors.add(config)
            }

            val executorsJson = json.encodeToString(currentExecutors)
            preferences[EXECUTORS_KEY] = executorsJson
        }
    }

    override suspend fun updateExecutor(config: PromptExecutorConfig) {
        promptExecutorDataStore.edit { preferences ->
            val currentExecutors = getAllExecutorConfigs().toMutableList()
            val index = currentExecutors.indexOfFirst { it.executorType == config.executorType }

            if (index != -1) {
                currentExecutors[index] = config
                val executorsJson = json.encodeToString(currentExecutors)
                preferences[EXECUTORS_KEY] = executorsJson
            } else {
                Log.w(TAG, "Attempted to update non-existent executor: ${config.executorType}")
            }
        }
    }

    override suspend fun deleteExecutor(type: ExecutorType) {
        promptExecutorDataStore.edit { preferences ->
            val currentExecutors = getAllExecutorConfigs().toMutableList()
            currentExecutors.removeAll { it.executorType == type }

            val executorsJson = json.encodeToString(currentExecutors)
            preferences[EXECUTORS_KEY] = executorsJson

            // If we're deleting the currently selected executor, clear the selection
            if (preferences[SELECTED_EXECUTOR_KEY] == type.name) {
                preferences.remove(SELECTED_EXECUTOR_KEY)
            }
        }
    }

    override suspend fun selectExecutor(type: ExecutorType) {
        promptExecutorDataStore.edit { preferences ->
            // Verify the executor exists before selecting it
            val executor = getExecutorByType(type)
            if (executor != null) {
                preferences[SELECTED_EXECUTOR_KEY] = type.name
            } else {
                Log.w(TAG, "Attempted to select non-existent executor: $type")
            }
        }
    }

    override suspend fun deleteAllExecutors() {
        promptExecutorDataStore.edit { preferences ->
            preferences.remove(EXECUTORS_KEY)
            preferences.remove(SELECTED_EXECUTOR_KEY)
        }
    }

    companion object {
        private const val TAG = "PromptExecutorRepository"
        private val EXECUTORS_KEY = stringPreferencesKey("executors")
        private val SELECTED_EXECUTOR_KEY = stringPreferencesKey("selected_executor")
    }
}
