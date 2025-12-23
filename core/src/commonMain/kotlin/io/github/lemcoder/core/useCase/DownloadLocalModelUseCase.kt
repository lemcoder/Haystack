package io.github.lemcoder.core.useCase

import io.github.lemcoder.core.utils.BaseLocalModel
import io.github.lemcoder.koog.edge.LocalModelDownloader
import io.github.lemcoder.koog.edge.downloadCactusModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DownloadLocalModelUseCase {
  suspend operator fun invoke(): Flow<Float>

  companion object {
    fun create(): DownloadLocalModelUseCase {
      return DownloadLocalModelUseCaseImpl()
    }
  }
}

private class DownloadLocalModelUseCaseImpl() : DownloadLocalModelUseCase {
  override suspend fun invoke(): Flow<Float> =
    withContext(Dispatchers.IO) { LocalModelDownloader.downloadCactusModel(BaseLocalModel) }
}
