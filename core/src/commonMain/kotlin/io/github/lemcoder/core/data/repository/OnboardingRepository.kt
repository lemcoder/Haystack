package io.github.lemcoder.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.github.lemcoder.core.model.OnboardingState
import io.github.lemcoder.core.platform.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val onboardingDataStore: DataStore<Preferences> by lazy {
    createDataStore { "onboarding.preferences_pb" }
}

interface OnboardingRepository {
    val onboardingStateFlow: Flow<OnboardingState>

    suspend fun markSampleNeedlesAdded()

    companion object {
        val Instance: OnboardingRepository by lazy { OnboardingRepositoryImpl() }
    }
}

class OnboardingRepositoryImpl() : OnboardingRepository {

    override val onboardingStateFlow: Flow<OnboardingState> =
        onboardingDataStore.data.map { preferences ->
            OnboardingState(didAddSampleNeedles = preferences[DID_ADD_SAMPLE_NEEDLES] ?: false)
        }

    override suspend fun markSampleNeedlesAdded() {
        onboardingDataStore.edit { preferences -> preferences[DID_ADD_SAMPLE_NEEDLES] = true }
    }

    companion object {
        private val DID_ADD_SAMPLE_NEEDLES = booleanPreferencesKey("did_add_sample_needles")
    }
}
