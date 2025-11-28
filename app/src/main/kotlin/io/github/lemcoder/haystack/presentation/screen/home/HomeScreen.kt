package io.github.lemcoder.haystack.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import io.github.lemcoder.haystack.designSystem.icons.IcAdd
import io.github.lemcoder.haystack.designSystem.icons.IcSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Empty title for centered content
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(42.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = {
                                onEvent(HomeEvent.OpenNeedles)
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IcAdd,
                            contentDescription = "Go to needles",
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(42.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable(onClick = {
                                onEvent(HomeEvent.OpenSettings)
                            }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IcSettings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { onEvent(HomeEvent.GenerateChart) },
                enabled = !state.isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (state.isGenerating) "Generating..." else "Generate Chart",
                    modifier = Modifier.padding(8.dp)
                )
            }

            when {
                state.isGenerating -> {
                    CircularProgressIndicator()
                }

                state.chartBitmap != null -> {
                    Image(
                        bitmap = state.chartBitmap.asImageBitmap(),
                        contentDescription = "Generated Chart",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                else -> {
                    Text(
                        text = "No chart generated yet. Click the button to generate one.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
