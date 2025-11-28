package io.github.lemcoder.haystack.presentation.screen.home

import android.graphics.Bitmap

data class HomeState(
    val chartBitmap: Bitmap? = null,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)
