package io.github.lemcoder.haystack.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.lemcoder.haystack.designSystem.component.toast.ToastHost
import io.github.lemcoder.haystack.designSystem.theme.HaystackTheme
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.screen.executorEdit.ExecutorEditRoute
import io.github.lemcoder.haystack.presentation.screen.executorSettings.ExecutorSettingsRoute
import io.github.lemcoder.haystack.presentation.screen.home.HomeRoute
import io.github.lemcoder.haystack.presentation.screen.needleDetail.NeedleDetailRoute
import io.github.lemcoder.haystack.presentation.screen.needles.NeedlesRoute
import io.github.lemcoder.haystack.presentation.screen.settings.SettingsRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent { HaystackTheme { MainScreen() } }
    }
}

@Composable
fun MainScreen() {
    val navigationService = remember { NavigationService.Instance }
    val destination by navigationService.destinationFlow.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (destination) {
            Destination.Home -> HomeRoute()
            Destination.Settings -> SettingsRoute()
            Destination.ExecutorSettings -> ExecutorSettingsRoute()
            is Destination.ExecutorEdit -> ExecutorEditRoute()
            Destination.Needles -> NeedlesRoute()
            is Destination.NeedleDetail -> NeedleDetailRoute()
        }
    }

    CustomBackHandler(navigationService)
    ToastHost()
}

@Composable
private fun CustomBackHandler(navigationService: NavigationService) {
    val activity = LocalActivity.current
    BackHandler {
        val success = navigationService.navigateBack()
        if (!success) {
            activity?.moveTaskToBack(true)
        }
    }
}
