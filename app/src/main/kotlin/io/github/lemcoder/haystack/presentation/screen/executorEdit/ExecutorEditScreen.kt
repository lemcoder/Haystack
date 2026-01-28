package io.github.lemcoder.haystack.presentation.screen.executorEdit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.lemcoder.core.model.llm.ExecutorType
import io.github.lemcoder.haystack.util.ExecutorTypeVariants
import io.github.lemcoder.haystack.util.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutorEditScreen(
    state: ExecutorEditState,
    onEvent: (ExecutorEditEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Executor" else "Add Executor") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ExecutorEditEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(ExecutorEditEvent.SaveExecutor) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        },
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    ExecutorEditForm(
                        state = state,
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExecutorEditForm(
    state: ExecutorEditState,
    onEvent: (ExecutorEditEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Executor Type Dropdown
        var executorTypeExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = executorTypeExpanded,
            onExpandedChange = { executorTypeExpanded = !executorTypeExpanded },
        ) {
            OutlinedTextField(
                value = state.executorType?.displayName() ?: "Select Executor Type",
                onValueChange = {},
                readOnly = true,
                label = { Text("Executor Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = executorTypeExpanded)
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                enabled = !state.isEditMode, // Disable in edit mode
            )

            ExposedDropdownMenu(
                expanded = executorTypeExpanded,
                onDismissRequest = { executorTypeExpanded = false },
            ) {
                ExecutorTypeVariants.all().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName()) },
                        onClick = {
                            onEvent(ExecutorEditEvent.UpdateExecutorType(type))
                            executorTypeExpanded = false
                        },
                    )
                }
            }
        }

        if (state.isEditMode) {
            Text(
                text = "Note: Executor type cannot be changed in edit mode",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Model Name Field
        OutlinedTextField(
            value = state.selectedModelName,
            onValueChange = { onEvent(ExecutorEditEvent.UpdateModelName(it)) },
            label = { Text("Model Name *") },
            placeholder = {
                Text(
                    when (state.executorType) {
                        is ExecutorType.OpenAI -> "e.g., gpt-4, gpt-3.5-turbo"
                        is ExecutorType.OpenRouter -> "e.g., anthropic/claude-3"
                        is ExecutorType.Ollama -> "e.g., llama2, mistral"
                        is ExecutorType.Local -> "e.g., local-model-name"
                        null -> "Enter model name"
                    }
                )
            },
            supportingText = { Text("Required field") },
            modifier = Modifier.fillMaxWidth(),
        )

        // API Key Field (for OpenAI and OpenRouter)
        when (state.executorType) {
            is ExecutorType.OpenAI,
            is ExecutorType.OpenRouter -> {
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = { onEvent(ExecutorEditEvent.UpdateApiKey(it)) },
                    label = { Text("API Key *") },
                    placeholder = { Text("Enter API key") },
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = { Text("Required: API key for authentication") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            else -> {
                // No API key field for Ollama or Local
            }
        }

        // Base URL Field (for Ollama)
        when (state.executorType) {
            is ExecutorType.OpenAI,
            is ExecutorType.Ollama -> {
                OutlinedTextField(
                    value = state.baseUrl,
                    onValueChange = { onEvent(ExecutorEditEvent.UpdateBaseUrl(it)) },
                    label = { Text("Base URL") },
                    placeholder = { Text("http://localhost:11434") },
                    supportingText = { Text("Optional: Defaults to http://localhost:11434") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            else -> {
                // No base URL field for other executors
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Help text
        Text(
            text = "* Required fields",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Executor type specific information
        state.executorType?.let { type ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = getExecutorTypeInfo(type),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun getExecutorTypeInfo(executorType: ExecutorType): String {
    return when (executorType) {
        is ExecutorType.OpenAI ->
            "OpenAI executors use models like GPT-4, GPT-3.5-turbo. Requires an OpenAI API key."
        is ExecutorType.OpenRouter ->
            "OpenRouter provides access to multiple AI models. Requires an OpenRouter API key."
        is ExecutorType.Ollama ->
            "Ollama runs models locally on your device. No API key required if running locally."
        is ExecutorType.Local -> "Local executors use models running on your local machine."
    }
}
