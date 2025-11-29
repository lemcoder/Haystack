package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.NeedleRepository
import io.github.lemcoder.haystack.core.data.samples.CalculateSumNeedle
import io.github.lemcoder.haystack.core.data.samples.ChartGeneratorNeedle
import io.github.lemcoder.haystack.core.data.samples.DataVisualizerNeedle
import io.github.lemcoder.haystack.core.data.samples.ListSorterNeedle
import io.github.lemcoder.haystack.core.data.samples.SampleNeedle
import io.github.lemcoder.haystack.core.data.samples.TemperatureConverterNeedle
import io.github.lemcoder.haystack.core.data.samples.TextAnalyzerNeedle
import io.github.lemcoder.haystack.core.data.samples.WeatherFetcherNeedle

class CreateSampleNeedlesUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    private val sampleNeedles: List<SampleNeedle> = listOf(
        CalculateSumNeedle,
        TextAnalyzerNeedle,
        TemperatureConverterNeedle,
        ListSorterNeedle,
        ChartGeneratorNeedle,
        WeatherFetcherNeedle,
        DataVisualizerNeedle
    )

    suspend operator fun invoke() {
        // Clear all existing needles and recreate samples
        // This ensures we always have the latest sample needles
        needleRepository.deleteAllNeedles()

        sampleNeedles.forEach { sampleNeedle ->
            needleRepository.saveNeedle(sampleNeedle.create())
        }
    }
}

