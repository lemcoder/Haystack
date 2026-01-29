package io.github.lemcoder.haystack.designSystem.component.toast

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

enum class ToastStyle {
    Success, Error;

    @Composable
    @ReadOnlyComposable
    internal fun getBackgroundColor(): Color = when (this) {
        Success -> MaterialTheme.colorScheme.tertiaryContainer
        Error -> MaterialTheme.colorScheme.errorContainer
    }
}