package io.github.lemcoder.haystack.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import io.github.lemcoder.haystack.designSystem.theme.HaystackTheme
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.screen.download.DownloadModelRoute
import io.github.lemcoder.haystack.presentation.screen.home.HomeRoute
import io.github.lemcoder.haystack.presentation.screen.needleDetail.NeedleDetailRoute
import io.github.lemcoder.haystack.presentation.screen.needleGenerator.NeedleGeneratorRoute
import io.github.lemcoder.haystack.presentation.screen.needles.NeedlesRoute
import io.github.lemcoder.haystack.presentation.screen.settings.SettingsRoute
import io.github.lemcoder.haystack.util.SnackbarUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        setContent { HaystackTheme { MainScreen() } }
    }
}

@Composable
fun MainScreen() {
    val navigationService = remember { NavigationService.Instance }
    val destination by navigationService.destinationFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarHostState) { SnackbarUtil.snackbarHostState = snackbarHostState }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    // This padding parameter is not used because we handle padding in individual screens
    // Scaffold is for snackbar hosting only here
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { _ ->
        AnimatedContent(targetState = destination, modifier = Modifier.fillMaxSize()) { destination
            ->
            when (destination) {
                Destination.DownloadModel -> DownloadModelRoute()
                Destination.Home -> HomeRoute()
                Destination.Settings ->
                    SettingsRoute(onNavigateBack = { navigationService.navigateBack() })
                Destination.Needles -> NeedlesRoute()
                is Destination.NeedleDetail -> NeedleDetailRoute(needleId = destination.needleId)
                Destination.NeedleGenerator -> NeedleGeneratorRoute()
            }
        }
    }

    BackHandler { navigationService.navigateBack() }
}
