package io.github.lemcoder.haystack.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.lemcoder.haystack.designSystem.theme.HaystackTheme

enum class SettingsListItemType {
    TOP,
    MIDDLE,
    BOTTOM
}

@Composable
internal fun SettingsListItem(
    type: SettingsListItemType,
    title: String,
    description: String,
    icon: @Composable (() -> Unit),
    onClick: () -> Unit,
) {
    val shape = getItemShape(type)

    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = icon,
        colors = ListItemDefaults.colors().copy(
             containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                // .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, shape)
                .clickable(onClick = onClick),
    )
}

@Composable
internal fun SettingsListItemIcon(
    color: Color,
    imageVector: ImageVector
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .border(1.dp, color, CircleShape)
            .background(color),
            // .innerShadow(CircleShape, Shadow(8.dp, color)),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
fun SettingsListItemPreview() {
    HaystackTheme {
        Column {
            SettingsListItem(
                type = SettingsListItemType.TOP,
                title = "Top",
                description = "Settings",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                    )
                },
                onClick = {},
            )
            SettingsListItem(
                type = SettingsListItemType.MIDDLE,
                title = "Middle",
                description = "Settings",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                    )
                },
                onClick = {},
            )
            SettingsListItem(
                type = SettingsListItemType.BOTTOM,
                title = "Bottom",
                description = "Settings",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon",
                    )
                },
                onClick = {},
            )
        }
    }
}

private fun getItemShape(type: SettingsListItemType) =
    when (type) {
        SettingsListItemType.TOP -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 2.dp,
            bottomEnd = 2.dp,
        )

        SettingsListItemType.MIDDLE -> RoundedCornerShape(2.dp)
        SettingsListItemType.BOTTOM -> RoundedCornerShape(
            topStart = 2.dp,
            topEnd = 2.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp,
        )
    }