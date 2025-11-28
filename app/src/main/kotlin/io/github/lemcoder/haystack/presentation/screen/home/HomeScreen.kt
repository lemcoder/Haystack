package io.github.lemcoder.haystack.presentation.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
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
