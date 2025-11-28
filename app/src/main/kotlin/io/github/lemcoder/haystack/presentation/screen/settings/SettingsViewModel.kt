package io.github.lemcoder.haystack.presentation.screen.settings

import androidx.lifecycle.viewModelScope
import com.cactus.InferenceMode
import io.github.lemcoder.haystack.core.data.SettingsRepository
import io.github.lemcoder.haystack.core.model.llm.ModelSettings
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository = SettingsRepository.Instance,
) : MviViewModel<SettingsState, SettingsEvent>() {
    private val _state = MutableStateFlow(SettingsState())
    override val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                settingsRepository.settingsFlow.collect { settings ->
                    _state.value = _state.value.copy(
                        temperature = settings.temperature?.toString() ?: "",
                        maxTokens = settings.maxTokens?.toString() ?: "",
                        topK = settings.topK?.toString() ?: "",
                        topP = settings.topP?.toString() ?: "",
                        stopSequences = settings.stopSequences.joinToString(", "),
                        cactusToken = settings.cactusToken ?: "",
                        inferenceMode = settings.inferenceMode.name,
                        allowInternetAccess = settings.allowInternetAccess,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading settings: ${e.message}"
                )
            }
        }
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.UpdateTemperature -> {
                _state.value = _state.value.copy(temperature = event.value)
            }

            is SettingsEvent.UpdateMaxTokens -> {
                _state.value = _state.value.copy(maxTokens = event.value)
            }

            is SettingsEvent.UpdateTopK -> {
                _state.value = _state.value.copy(topK = event.value)
            }

            is SettingsEvent.UpdateTopP -> {
                _state.value = _state.value.copy(topP = event.value)
            }

            is SettingsEvent.UpdateStopSequences -> {
                _state.value = _state.value.copy(stopSequences = event.value)
            }

            is SettingsEvent.UpdateCactusToken -> {
                _state.value = _state.value.copy(cactusToken = event.value)
            }

            is SettingsEvent.UpdateInferenceMode -> {
                _state.value = _state.value.copy(inferenceMode = event.mode)
            }

            is SettingsEvent.UpdateInternetAccess -> {
                _state.value = _state.value.copy(allowInternetAccess = event.enabled)
            }

            SettingsEvent.SaveSettings -> saveSettings()
        }
    }

    private fun saveSettings() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true, errorMessage = null)

                val settings = ModelSettings(
                    temperature = _state.value.temperature.toDoubleOrNull(),
                    maxTokens = _state.value.maxTokens.toIntOrNull(),
                    topK = _state.value.topK.toIntOrNull(),
                    topP = _state.value.topP.toDoubleOrNull(),
                    stopSequences = _state.value.stopSequences
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() },
                    cactusToken = _state.value.cactusToken.takeIf { it.isNotBlank() },
                    inferenceMode = try {
                        InferenceMode.valueOf(_state.value.inferenceMode)
                    } catch (e: IllegalArgumentException) {
                        InferenceMode.LOCAL
                    },
                    allowInternetAccess = _state.value.allowInternetAccess
                )

                settingsRepository.saveSettings(settings)

                _state.value = _state.value.copy(isSaving = false)
                SnackbarUtil.showSnackbar("Settings saved successfully")
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    errorMessage = "Error saving settings: ${e.message}"
                )
                SnackbarUtil.showSnackbar("Error saving settings: ${e.message ?: "Unknown error"}")
            }
        }
    }
}
