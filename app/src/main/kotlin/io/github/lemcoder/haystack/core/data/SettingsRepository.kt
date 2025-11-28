package io.github.lemcoder.haystack.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cactus.InferenceMode
import io.github.lemcoder.koog.edge.cactus.CactusLLMParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class ModelSettings(
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val topK: Int? = null,
    val topP: Double? = null,
    val stopSequences: List<String> = emptyList(),
    val cactusToken: String? = null,
    val inferenceMode: InferenceMode = InferenceMode.LOCAL,
    val allowInternetAccess: Boolean = false
)

class SettingsRepository(private val context: Context) {

    companion object {
        private val TEMPERATURE = doublePreferencesKey("temperature")
        private val MAX_TOKENS = intPreferencesKey("max_tokens")
        private val TOP_K = intPreferencesKey("top_k")
        private val TOP_P = doublePreferencesKey("top_p")
        private val STOP_SEQUENCES = stringPreferencesKey("stop_sequences")
        private val CACTUS_TOKEN = stringPreferencesKey("cactus_token")
        private val INFERENCE_MODE = stringPreferencesKey("inference_mode")
        private val ALLOW_INTERNET_ACCESS = booleanPreferencesKey("allow_internet_access")
    }

    val settingsFlow: Flow<ModelSettings> = context.dataStore.data.map { preferences ->
        ModelSettings(
            temperature = preferences[TEMPERATURE],
            maxTokens = preferences[MAX_TOKENS],
            topK = preferences[TOP_K],
            topP = preferences[TOP_P],
            stopSequences = preferences[STOP_SEQUENCES]?.split(",")?.filter { it.isNotBlank() }
                ?: emptyList(),
            cactusToken = preferences[CACTUS_TOKEN],
            inferenceMode = preferences[INFERENCE_MODE]?.let {
                try {
                    InferenceMode.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    InferenceMode.LOCAL
                }
            } ?: InferenceMode.LOCAL,
            allowInternetAccess = preferences[ALLOW_INTERNET_ACCESS] ?: false
        )
    }

    suspend fun saveSettings(settings: ModelSettings) {
        context.dataStore.edit { preferences ->
            settings.temperature?.let { preferences[TEMPERATURE] = it } ?: preferences.remove(
                TEMPERATURE
            )
            settings.maxTokens?.let { preferences[MAX_TOKENS] = it } ?: preferences.remove(
                MAX_TOKENS
            )
            settings.topK?.let { preferences[TOP_K] = it } ?: preferences.remove(TOP_K)
            settings.topP?.let { preferences[TOP_P] = it } ?: preferences.remove(TOP_P)

            if (settings.stopSequences.isNotEmpty()) {
                preferences[STOP_SEQUENCES] = settings.stopSequences.joinToString(",")
            } else {
                preferences.remove(STOP_SEQUENCES)
            }

            settings.cactusToken?.let { preferences[CACTUS_TOKEN] = it } ?: preferences.remove(
                CACTUS_TOKEN
            )
            preferences[INFERENCE_MODE] = settings.inferenceMode.name
            preferences[ALLOW_INTERNET_ACCESS] = settings.allowInternetAccess
        }
    }

    fun toCactusLLMParams(settings: ModelSettings): CactusLLMParams {
        return CactusLLMParams(
            temperature = settings.temperature,
            maxTokens = settings.maxTokens,
            topK = settings.topK,
            topP = settings.topP,
            stopSequences = settings.stopSequences,
            cactusToken = settings.cactusToken,
            inferenceMode = settings.inferenceMode
        )
    }
}
