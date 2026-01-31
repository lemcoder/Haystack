package io.github.lemcoder.haystack.presentation.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import io.github.lemcoder.haystack.R
import io.github.lemcoder.haystack.designSystem.theme.HaystackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { onEvent(SettingsEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                SettingsListItem(
                    title = stringResource(R.string.settings_executor_title),
                    description = stringResource(R.string.settings_executor_description),
                    type = SettingsListItemType.TOP,
                    icon = {
                        SettingsListItemIcon(
                            color = Color(0xFF4CAFFF),
                            imageVector = Icons.Default.Settings
                        )
                    },
                    onClick = { onEvent(SettingsEvent.NavigateToExecutorSettings) }
                )
            }
            item {
                SettingsListItem(
                    title = stringResource(R.string.settings_local_tools_title),
                    description = stringResource(R.string.settings_local_tools_description),
                    type = SettingsListItemType.BOTTOM,
                    icon = {
                        SettingsListItemIcon(
                            color = Color(0xFFEB4A1A),
                            imageVector = Icons.Default.Settings
                        )
                    },
                    onClick = { onEvent(SettingsEvent.NavigateToNeedleManagement) }
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun SettingsScreenPreview() {
    HaystackTheme {
        SettingsScreen(
            state = SettingsState(),
            onEvent = {},
        )
    }
}

