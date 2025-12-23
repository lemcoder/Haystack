package io.github.lemcoder.haystack.core.useCase

import android.content.Context
import io.github.lemcoder.haystack.App
import io.github.lemcoder.haystack.util.BaseLocalModel

interface CheckIfModelDownloadedUseCase {
  operator fun invoke(): Boolean

  companion object {
    fun create(): CheckIfModelDownloadedUseCase {
      return CheckIfModelDownloadedUseCaseImpl()
    }
  }
}

private class CheckIfModelDownloadedUseCaseImpl() : CheckIfModelDownloadedUseCase {
  private val context: Context = App.context

  override fun invoke(): Boolean {
    val filesDir = context.filesDir
    val modelFile = filesDir.resolve("models/${BaseLocalModel.id}")
    return modelFile.exists()
  }
}
