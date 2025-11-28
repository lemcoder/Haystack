package io.github.lemcoder.haystack.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.screen.download.DownloadModelRoute
import io.github.lemcoder.haystack.presentation.screen.home.HomeRoute
import io.github.lemcoder.haystack.presentation.screen.settings.SettingsRoute
import io.github.lemcoder.haystack.util.SnackbarUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                MainScreen()
            }
        }
    }
}


@Composable
fun MainScreen() {
    val navigationService = remember { NavigationService.Instance }
    val destination by navigationService.destinationFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarHostState) {
        SnackbarUtil.snackbarHostState = snackbarHostState
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        AnimatedContent(
            targetState = destination,
            modifier = Modifier.padding(innerPadding)
        ) { destination ->
            when (destination) {
                Destination.DownloadModel -> DownloadModelRoute()
                Destination.Home -> HomeRoute()
                Destination.Settings -> SettingsRoute()
                Destination.Needles -> TODO()
            }
        }
    }

    BackHandler {
        navigationService.navigateBack()
    }
}
