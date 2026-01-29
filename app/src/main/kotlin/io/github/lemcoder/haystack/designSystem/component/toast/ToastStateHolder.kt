package io.github.lemcoder.haystack.designSystem.component.toast

import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val DefaultDuration = 2.seconds

object Toast {
    private val _toastState = MutableStateFlow(ToastState())
    internal val toastState: StateFlow<ToastState> = _toastState

    private var toastJob: Job? = null
    private var onClick: (() -> Unit)? = null

    internal val clickEnabled: Boolean
        get() = onClick != null

    fun show(
        message: String,
        style: ToastStyle = ToastStyle.Success,
        duration: Duration = DefaultDuration,
        onClick: (() -> Unit)? = null,
    ) = show(
        message = ToastMessage.Text(message),
        style = style,
        duration = duration,
        onClick = onClick,
    )

    fun show(
        message: AnnotatedString,
        style: ToastStyle = ToastStyle.Success,
        duration: Duration = DefaultDuration,
        onClick: (() -> Unit)? = null,
    ) = show(
        message = ToastMessage.Annotated(message),
        style = style,
        duration = duration,
        onClick = onClick,
    )

    fun show(
        @StringRes message: Int,
        style: ToastStyle = ToastStyle.Success,
        duration: Duration = DefaultDuration,
        onClick: (() -> Unit)? = null,
    ) = show(
        message = ToastMessage.Resource(message),
        style = style,
        duration = duration,
        onClick = onClick,
    )

    internal fun onToastClick() {
        onClick?.invoke()
        cancelToast()
    }

    private fun show(
        message: ToastMessage,
        style: ToastStyle,
        duration: Duration,
        onClick: (() -> Unit)? = null,
    ) {
        cancelToast()
        this.onClick = onClick
        _toastState.value = ToastState(message, style, visible = true)
        toastJob = CoroutineScope(Dispatchers.Main).launch {
            delay(duration)
            cancelToast()
        }
    }

    private fun cancelToast() {
        _toastState.update { it.copy(visible = false) }
        onClick = null
        toastJob?.cancel()
    }
}