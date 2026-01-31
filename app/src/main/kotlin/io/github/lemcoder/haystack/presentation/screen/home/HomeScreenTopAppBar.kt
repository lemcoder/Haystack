package io.github.lemcoder.haystack.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.lemcoder.haystack.R
import io.github.lemcoder.haystack.designSystem.component.RoundButton
import io.github.lemcoder.haystack.designSystem.icons.IcSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTopAppBar(
    availableTools: Int,
    canClearChat: Boolean,
    onChatCleared: () -> Unit,
    onSettingsOpened: () -> Unit,
) {
    TopAppBar(
        title = {
            if (availableTools > 0) {
                Text(
                    text = stringResource(R.string.top_bar_tools_available, availableTools),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            AnimatedVisibility(canClearChat) {
                RoundButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Clear chat",
                    onClick = onChatCleared,
                    enabled = canClearChat,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(42.dp),
                )
            }
        },
        navigationIcon = {
            RoundButton(
                icon = IcSettings,
                contentDescription = "Clear chat",
                onClick = onSettingsOpened,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(42.dp),
            )
        },
    )
}

@Composable
@Preview
private fun HomeScreenTopAppBarPreview() {
    HomeScreenTopAppBar(
        availableTools = 3,
        canClearChat = true,
        onChatCleared = {},
        onSettingsOpened = {},
    )
}