package io.github.lemcoder.haystack.presentation.screen.needleDetail

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.presentation.screen.needleDetail.component.PythonCodeView
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeedleDetailScreen(
    state: NeedleDetailState,
    onEvent: (NeedleDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.needle?.name ?: "Needle Detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(NeedleDetailEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.needle != null) {
                FloatingActionButton(
                    onClick = {
                        if (!state.isExecuting) {
                            onEvent(NeedleDetailEvent.ExecuteNeedle)
                        }
                    }
                ) {
                    if (state.isExecuting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Execute")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.needle != null -> {
                    NeedleDetailContent(
                        needle = state.needle,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    Text(
                        text = "Needle not found",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Arguments Dialog
            if (state.showArgumentsDialog && state.needle != null) {
                ArgumentsDialog(
                    needle = state.needle,
                    argumentValues = state.argumentValues,
                    onArgumentChange = { name, value ->
                        onEvent(NeedleDetailEvent.UpdateArgument(name, value))
                    },
                    onConfirm = { onEvent(NeedleDetailEvent.ConfirmAndExecute) },
                    onDismiss = { onEvent(NeedleDetailEvent.DismissArgumentsDialog) }
                )
            }

            // Result Dialog
            state.executionResult?.let { result ->
                when (result) {
                    is ExecutionResult.TextResult -> {
                        TextResultDialog(
                            output = result.output,
                            onDismiss = { onEvent(NeedleDetailEvent.DismissResult) }
                        )
                    }

                    is ExecutionResult.ImageResult -> {
                        ImageResultDialog(
                            imagePath = result.imagePath,
                            onDismiss = { onEvent(NeedleDetailEvent.DismissResult) }
                        )
                    }

                    is ExecutionResult.ErrorResult -> {
                        ErrorResultDialog(
                            error = result.error,
                            onDismiss = { onEvent(NeedleDetailEvent.DismissResult) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeedleDetailContent(
    needle: Needle,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Metadata Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = needle.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (needle.args.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Arguments:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    needle.args.forEach { arg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = arg.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "${arg.type}${if (!arg.required) " (optional)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (arg.description.isNotBlank()) {
                            Text(
                                text = arg.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Code Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Text(
                    text = "Python Code",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                PythonCodeView(
                    code = needle.pythonCode,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ArgumentsDialog(
    needle: Needle,
    argumentValues: Map<String, String>,
    onArgumentChange: (String, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Arguments") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                needle.args.forEach { arg ->
                    OutlinedTextField(
                        value = argumentValues[arg.name] ?: "",
                        onValueChange = { onArgumentChange(arg.name, it) },
                        label = {
                            Text("${arg.name}${if (arg.required) " *" else ""}")
                        },
                        supportingText = {
                            Text("${arg.type}${if (arg.description.isNotBlank()) " - ${arg.description}" else ""}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Execute")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TextResultDialog(
    output: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Execution Result") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = output,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ImageResultDialog(
    imagePath: String,
    onDismiss: () -> Unit
) {
    val bitmap = BitmapFactory.decodeFile(imagePath)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Execution Result") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Result Image",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Failed to load image")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ErrorResultDialog(
    error: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Execution Error") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = error,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
