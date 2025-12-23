package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.repository.NeedleRepository
import io.github.lemcoder.haystack.core.data.samples.CameraCaptureNeedle
import io.github.lemcoder.haystack.core.data.samples.CryptoChartGeneratorNeedle
import io.github.lemcoder.haystack.core.data.samples.SampleNeedle
import io.github.lemcoder.haystack.core.data.samples.WeatherFetcherNeedle

class CreateSampleNeedlesUseCase(
  private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
  private val sampleNeedles: List<SampleNeedle> =
    listOf(CryptoChartGeneratorNeedle, CameraCaptureNeedle, WeatherFetcherNeedle)

  suspend operator fun invoke() {
    // Clear all existing needles and recreate samples
    // This ensures we always have the latest sample needles
    needleRepository.deleteAllNeedles()

    sampleNeedles.forEach { sampleNeedle -> needleRepository.saveNeedle(sampleNeedle.create()) }
  }
}
