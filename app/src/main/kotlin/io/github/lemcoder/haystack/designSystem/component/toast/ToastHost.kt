package io.github.lemcoder.haystack.designSystem.component.toast

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider

@Composable
fun ToastHost() {
    val toastState by Toast.toastState.collectAsState()

    if (toastState.visible) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnClickOutside = false,
                dismissOnBackPress = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
        ) {
            (LocalView.current.parent as DialogWindowProvider).window.apply {
                setGravity(Gravity.TOP)
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
                addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            }

            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300),
                ),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300),
                ),
                modifier = Modifier
                    .systemBarsPadding()
                    .wrapContentHeight(Alignment.Top),
            ) {
                when (val message = toastState.message) {
                    is ToastMessage.Annotated -> {
                        ToastText(
                            style = toastState.style,
                            text = message.value,
                            modifier = Modifier.toastPositioning(),
                        )
                    }
                    is ToastMessage.Text -> {
                        ToastText(
                            style = toastState.style,
                            text = message.value,
                            modifier = Modifier.toastPositioning(),
                        )
                    }
                    is ToastMessage.Resource -> {
                        ToastText(
                            style = toastState.style,
                            text = stringResource(message.resId),
                            modifier = Modifier.toastPositioning(),
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.toastPositioning(): Modifier = statusBarsPadding()
    .padding(horizontal = 16.dp)
    .wrapContentSize(Alignment.TopCenter)