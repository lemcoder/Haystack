package io.github.lemcoder.haystack.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Model Parameters",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = state.temperature,
                    onValueChange = { onEvent(SettingsEvent.UpdateTemperature(it)) },
                    label = { Text("Temperature") },
                    placeholder = { Text("e.g., 0.7") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Controls randomness (0.0 - 1.0)") }
                )

                OutlinedTextField(
                    value = state.maxTokens,
                    onValueChange = { onEvent(SettingsEvent.UpdateMaxTokens(it)) },
                    label = { Text("Max Tokens") },
                    placeholder = { Text("e.g., 2048") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Maximum number of tokens to generate") }
                )

                OutlinedTextField(
                    value = state.topK,
                    onValueChange = { onEvent(SettingsEvent.UpdateTopK(it)) },
                    label = { Text("Top K") },
                    placeholder = { Text("e.g., 50") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Limits vocabulary to top K tokens") }
                )

                OutlinedTextField(
                    value = state.topP,
                    onValueChange = { onEvent(SettingsEvent.UpdateTopP(it)) },
                    label = { Text("Top P") },
                    placeholder = { Text("e.g., 0.9") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Nucleus sampling threshold (0.0 - 1.0)") }
                )

                OutlinedTextField(
                    value = state.stopSequences,
                    onValueChange = { onEvent(SettingsEvent.UpdateStopSequences(it)) },
                    label = { Text("Stop Sequences") },
                    placeholder = { Text("e.g., END, STOP") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Comma-separated list of stop sequences") },
                    minLines = 2
                )

                OutlinedTextField(
                    value = state.cactusToken,
                    onValueChange = { onEvent(SettingsEvent.UpdateCactusToken(it)) },
                    label = { Text("Cactus Token") },
                    placeholder = { Text("Optional API token") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("API token for Cactus service") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onEvent(SettingsEvent.SaveSettings) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (state.isSaving) "Saving..." else "Save Settings",
                        modifier = Modifier.padding(8.dp)
                    )
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
}
