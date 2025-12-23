package io.github.lemcoder.haystack.presentation.screen.home

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import io.github.lemcoder.core.model.chat.Message
import io.github.lemcoder.core.model.chat.MessageContentType
import io.github.lemcoder.core.model.chat.MessageRole
import io.github.lemcoder.haystack.designSystem.icons.IcNeedles
import io.github.lemcoder.haystack.designSystem.icons.IcSettings
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(state: HomeState, onEvent: (HomeEvent) -> Unit, modifier: Modifier = Modifier) {
  val listState = rememberLazyListState()

  // Auto-scroll to bottom when new messages arrive
  LaunchedEffect(state.messages.size) {
    if (state.messages.isNotEmpty()) {
      listState.animateScrollToItem(state.messages.size - 1)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(text = "Haystack", style = MaterialTheme.typography.titleLarge)
            if (state.availableNeedles.isNotEmpty()) {
              Text(
                text = "${state.availableNeedles.size} tools available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        },
        actions = {
          // Clear chat button
          IconButton(
            onClick = { onEvent(HomeEvent.ClearChat) },
            enabled = state.messages.isNotEmpty(),
          ) {
            Icon(Icons.Default.Delete, "Clear chat")
          }

          // Needles button
          Box(
            modifier =
              Modifier.padding(end = 8.dp)
                .size(42.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = { onEvent(HomeEvent.OpenNeedles) }),
            contentAlignment = Alignment.Center,
          ) {
            Icon(imageVector = IcNeedles, contentDescription = "Go to needles")
          }
        },
        navigationIcon = {
          Box(
            modifier =
              Modifier.padding(start = 8.dp)
                .size(42.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = { onEvent(HomeEvent.OpenSettings) }),
            contentAlignment = Alignment.Center,
          ) {
            Icon(imageVector = IcSettings, contentDescription = "Settings")
          }
        },
      )
    }
  ) { paddingValues ->
    Column(modifier = modifier.fillMaxSize().imePadding().padding(paddingValues)) {
      // Error message
      state.errorMessage?.let { error ->
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = MaterialTheme.colorScheme.errorContainer,
        ) {
          Text(
            text = error,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp),
          )
        }
      }

      // Messages
      LazyColumn(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (state.messages.isEmpty()) {
          item {
            Box(
              modifier = Modifier.fillParentMaxSize().padding(32.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = "Welcome to Haystack!",
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.primary,
                  textAlign = TextAlign.Center,
                )
                Text(
                  text = "Chat with your AI assistant and use your custom tools (Needles).",
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center,
                )

                if (state.availableNeedles.isNotEmpty()) {
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Available needles:",
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                  )
                  Text(
                    text = state.availableNeedles.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                  )
                }
              }
            }
          }
        }

        items(state.messages) { message ->
          when (message.role) {
            MessageRole.TOOL -> {
              ToolCallMessage(toolName = message.content)
            }

            MessageRole.TOOL_RESULT -> {
              ToolResultMessage(message = message)
            }

            else -> {
              MessageBubble(content = message.content, isUser = message.role == MessageRole.USER)
            }
          }
        }

        if (state.isProcessing && state.processingToolCalls.isEmpty()) {
          item { ProcessingIndicator(toolCalls = state.processingToolCalls) }
        }
      }

      // Input area
      Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(8.dp),
          verticalAlignment = Alignment.Bottom,
        ) {
          OutlinedTextField(
            value = state.currentInput,
            onValueChange = { onEvent(HomeEvent.UpdateInput(it)) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type your message...") },
            enabled = !state.isProcessing,
            maxLines = 4,
          )
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(
            onClick = { onEvent(HomeEvent.SendMessage) },
            enabled = state.currentInput.isNotBlank() && !state.isProcessing,
            modifier =
              Modifier.size(56.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.surfaceVariant),
          ) {
            Icon(
              Icons.Default.ArrowUpward,
              contentDescription = "Send",
              modifier = Modifier.size(32.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
fun MessageBubble(content: String, isUser: Boolean) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
  ) {
    Surface(
      shape =
        RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isUser) 16.dp else 4.dp,
          bottomEnd = if (isUser) 4.dp else 16.dp,
        ),
      color =
        if (isUser) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.secondaryContainer,
      modifier = Modifier.widthIn(max = 300.dp),
    ) {
      Text(
        text = content,
        modifier = Modifier.padding(12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color =
          if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
          else MaterialTheme.colorScheme.onSecondaryContainer,
      )
    }
  }
}

@Composable
fun ToolCallMessage(toolName: String, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.Center,
  ) {
    Row(
      modifier =
        Modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
          )
          .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = "🔧", style = MaterialTheme.typography.bodyMedium)
      Text(
        text = "Running: ${toolName.replace("_", " ").replaceFirstChar { it.uppercase() }}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun ProcessingIndicator(toolCalls: List<String>, modifier: Modifier = Modifier) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
    Surface(
      shape =
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
      color = MaterialTheme.colorScheme.tertiaryContainer,
      modifier = Modifier.widthIn(max = 300.dp),
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)

        Text(
          text = "Thinking...",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
      }
    }
  }
}

@Composable
fun ToolResultMessage(message: Message, modifier: Modifier = Modifier) {
  val context = LocalContext.current

  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
    Surface(
      shape =
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp),
      color = MaterialTheme.colorScheme.secondaryContainer,
      modifier = Modifier.widthIn(max = 300.dp),
    ) {
      Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (message.contentType) {
          MessageContentType.IMAGE -> {
            message.imagePath?.let { imagePath ->
              // Load image from file path
              val bitmap =
                remember(imagePath) {
                  try {
                    val file = File(imagePath)
                    if (file.exists()) {
                      BitmapFactory.decodeFile(imagePath)
                    } else {
                      null
                    }
                  } catch (e: Exception) {
                    null
                  }
                }

              if (bitmap != null) {
                Image(
                  bitmap = bitmap.asImageBitmap(),
                  contentDescription = "Tool result image",
                  modifier =
                    Modifier.fillMaxWidth()
                      .heightIn(max = 300.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { openImageInViewer(context, imagePath) },
                  contentScale = ContentScale.Fit,
                )
              } else {
                // If image loading fails, show the text content (file path)
                Text(
                  text = message.content,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
            }
              ?: run {
                Text(
                  text = message.content,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
              }
          }

          MessageContentType.TEXT -> {
            Text(
              text = message.content,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }

          MessageContentType.MIXED -> {
            // Display text
            Text(
              text = message.content,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )

            // Display image if available
            message.imagePath?.let { imagePath ->
              val bitmap =
                remember(imagePath) {
                  try {
                    val file = File(imagePath)
                    if (file.exists()) {
                      BitmapFactory.decodeFile(imagePath)
                    } else {
                      null
                    }
                  } catch (e: Exception) {
                    null
                  }
                }

              if (bitmap != null) {
                Image(
                  bitmap = bitmap.asImageBitmap(),
                  contentDescription = "Tool result image",
                  modifier =
                    Modifier.fillMaxWidth()
                      .heightIn(max = 300.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .clickable { openImageInViewer(context, imagePath) },
                  contentScale = ContentScale.Fit,
                )
              }
            }
          }
        }
      }
    }
  }
}

/** Opens an image in the system's default image viewer */
private fun openImageInViewer(context: android.content.Context, imagePath: String) {
  try {
    val file = File(imagePath)
    if (!file.exists()) {
      return
    }

    // Use FileProvider to get a content URI for the file
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    // Create an intent to view the image
    val intent =
      Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

    // Start the activity
    context.startActivity(intent)
  } catch (e: Exception) {
    // If opening fails, silently ignore (could add error handling here)
    android.util.Log.e("HomeScreen", "Failed to open image", e)
  }
}
