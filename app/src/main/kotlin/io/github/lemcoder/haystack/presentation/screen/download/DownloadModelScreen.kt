package io.github.lemcoder.haystack.presentation.screen.download

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import io.github.lemcoder.haystack.designSystem.component.NetworkNotAvailableBanner
import io.github.lemcoder.haystack.designSystem.icons.IcHaystackLogo

@Composable
fun DownloadModelScreen(
    state: DownloadModelState,
    onEvent: (DownloadModelEvent) -> Unit
) {
    // This screen shows a banner if network is not available
    // Network is needed to download the model
    NetworkNotAvailableBanner()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top spacer
                Box(modifier = Modifier.weight(1f))

                // Logo in center
                LogoSection(isLoading = state.isDownloading)

                // Bottom button section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { onEvent(DownloadModelEvent.StartDownload) },
                        enabled = !state.isDownloading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (state.isDownloading) "Downloading..." else "Download Model",
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    state.errorMessage?.let { error ->
                        Text(
                            text = error,
                            modifier = Modifier.padding(top = 16.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoSection(isLoading: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Image(
        imageVector = IcHaystackLogo,
        contentDescription = "Haystack Logo",
        modifier = Modifier
            .size(45.dp)
            .then(
                if (isLoading) {
                    Modifier.rotate(rotation)
                } else {
                    Modifier
                }
            ),
    )
}