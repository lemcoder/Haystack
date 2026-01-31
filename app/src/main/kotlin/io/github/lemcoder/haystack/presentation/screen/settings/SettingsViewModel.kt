package io.github.lemcoder.haystack.presentation.screen.settings

import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val navigationService: NavigationService = NavigationService.Instance
) : MviViewModel<SettingsState, SettingsEvent>() {
    private val _state = MutableStateFlow(SettingsState())
    override val state: StateFlow<SettingsState> = _state.asStateFlow()

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.NavigateBack -> {
                navigationService.navigateBack()
            }

            SettingsEvent.NavigateToExecutorSettings -> {
                navigationService.navigateTo(Destination.ExecutorSettings)
            }

            SettingsEvent.NavigateToNeedleManagement -> {
                navigationService.navigateTo(Destination.Needles)
            }
        }
    }
}
