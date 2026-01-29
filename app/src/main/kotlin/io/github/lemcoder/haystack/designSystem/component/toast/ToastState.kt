package io.github.lemcoder.haystack.designSystem.component.toast

import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString

internal data class ToastState(
    val message: ToastMessage = ToastMessage.Text(""),
    val style: ToastStyle = ToastStyle.Success,
    val visible: Boolean = false,
)

internal sealed interface ToastMessage {
    data class Text(val value: String) : ToastMessage

    data class Annotated(val value: AnnotatedString) : ToastMessage

    data class Resource(@param:StringRes val resId: Int) : ToastMessage
}
