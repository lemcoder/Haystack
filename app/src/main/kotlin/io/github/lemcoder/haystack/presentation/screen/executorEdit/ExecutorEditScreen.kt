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
                title = {
                    Text(if (state.isEditMode) "Edit Executor" else "Add Executor")
                },
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
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Executor Type Dropdown
        var executorTypeExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = executorTypeExpanded,
            onExpandedChange = { executorTypeExpanded = !executorTypeExpanded },
        ) {
            OutlinedTextField(
                value = state.executorType?.name ?: "Select Executor Type",
                onValueChange = {},
                readOnly = true,
                label = { Text("Executor Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = executorTypeExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                enabled = !state.isEditMode, // Disable in edit mode
            )

            ExposedDropdownMenu(
                expanded = executorTypeExpanded,
                onDismissRequest = { executorTypeExpanded = false },
            ) {
                ExecutorType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name) },
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
                        ExecutorType.OPEN_AI -> "e.g., gpt-4, gpt-3.5-turbo"
                        ExecutorType.OPEN_ROUTER -> "e.g., anthropic/claude-3"
                        ExecutorType.OLLAMA -> "e.g., llama2, mistral"
                        ExecutorType.LOCAL -> "e.g., local-model-name"
                        null -> "Enter model name"
                    }
                )
            },
            supportingText = { Text("Required field") },
            modifier = Modifier.fillMaxWidth(),
        )

        // API Key Field (optional for some executors)
        OutlinedTextField(
            value = state.apiKey,
            onValueChange = { onEvent(ExecutorEditEvent.UpdateApiKey(it)) },
            label = { Text("API Key") },
            placeholder = { Text("Enter API key if required") },
            visualTransformation = PasswordVisualTransformation(),
            supportingText = {
                Text(
                    when (state.executorType) {
                        ExecutorType.OLLAMA, ExecutorType.LOCAL ->
                            "Optional: API key may not be required"
                        ExecutorType.OPEN_AI, ExecutorType.OPEN_ROUTER ->
                            "Required: API key for authentication"
                        null -> "Optional"
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

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
        ExecutorType.OPEN_AI ->
            "OpenAI executors use models like GPT-4, GPT-3.5-turbo. Requires an OpenAI API key."
        ExecutorType.OPEN_ROUTER ->
            "OpenRouter provides access to multiple AI models. Requires an OpenRouter API key."
        ExecutorType.OLLAMA ->
            "Ollama runs models locally on your device. No API key required if running locally."
        ExecutorType.LOCAL -> "Local executors use models running on your local machine."
    }
}
