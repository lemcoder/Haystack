package io.github.lemcoder.haystack.designSystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.lemcoder.haystack.core.service.network.NetworkStatusService

/**
 * A non-dismissible dialog that appears when network is unavailable. Automatically dismisses when
 * network connection is restored.
 *
 * Usage:
 * ```
 * @Composable
 * fun MyScreen() {
 *     NetworkNotAvailableBanner()
 *
 *     // Your screen content
 * }
 * ```
 */
@Composable
fun NetworkNotAvailableBanner() {
  val isNetworkAvailable by
    NetworkStatusService.Instance.isNetworkAvailable.collectAsStateWithLifecycle(
      initialValue = true
    )

  AnimatedVisibility(visible = !isNetworkAvailable, enter = fadeIn(), exit = fadeOut()) {
    Dialog(
      onDismissRequest = { /* Not dismissible */ },
      properties =
        DialogProperties(
          dismissOnBackPress = false,
          dismissOnClickOutside = false,
          usePlatformDefaultWidth = false,
        ),
    ) {
      Card(
        modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
          ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      ) {
        Column(
          modifier = Modifier.padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Icon and Title Row
          Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "No Network",
              modifier = Modifier.size(32.dp),
              tint = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "No Internet Connection",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onErrorContainer,
            )
          }

          // Message
          Text(
            text =
              "This feature requires an active internet connection. Please check your network settings and try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
            modifier = Modifier.fillMaxWidth(),
          )

          // Loading indicator
          Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Waiting for connection...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
            )
          }
        }
      }
    }
  }
}
