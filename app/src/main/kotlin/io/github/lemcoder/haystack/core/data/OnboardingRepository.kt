package io.github.lemcoder.haystack.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import io.github.lemcoder.haystack.App
import io.github.lemcoder.haystack.core.model.OnboardingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

interface OnboardingRepository {
    val onboardingStateFlow: Flow<OnboardingState>
    suspend fun markSampleNeedlesAdded()

    companion object {
        val Instance: OnboardingRepository by lazy {
            OnboardingRepositoryImpl()
        }
    }
}

class OnboardingRepositoryImpl(
    private val context: Context = App.context
) : OnboardingRepository {

    override val onboardingStateFlow: Flow<OnboardingState> =
        context.onboardingDataStore.data.map { preferences ->
            OnboardingState(
                didAddSampleNeedles = preferences[DID_ADD_SAMPLE_NEEDLES] ?: false
            )
        }

    override suspend fun markSampleNeedlesAdded() {
        context.onboardingDataStore.edit { preferences ->
            preferences[DID_ADD_SAMPLE_NEEDLES] = true
        }
    }

    companion object {
        private val DID_ADD_SAMPLE_NEEDLES = booleanPreferencesKey("did_add_sample_needles")
    }
}
