package io.github.lemcoder.haystack.core.useCase

import io.github.lemcoder.haystack.core.data.NeedleRepository
import io.github.lemcoder.haystack.core.useCase.samples.CalculateSumNeedle
import io.github.lemcoder.haystack.core.useCase.samples.ChartGeneratorNeedle
import io.github.lemcoder.haystack.core.useCase.samples.ListSorterNeedle
import io.github.lemcoder.haystack.core.useCase.samples.SampleNeedle
import io.github.lemcoder.haystack.core.useCase.samples.TemperatureConverterNeedle
import io.github.lemcoder.haystack.core.useCase.samples.TextAnalyzerNeedle

class CreateSampleNeedlesUseCase(
    private val needleRepository: NeedleRepository = NeedleRepository.Instance
) {
    private val sampleNeedles: List<SampleNeedle> = listOf(
        CalculateSumNeedle,
        TextAnalyzerNeedle,
        TemperatureConverterNeedle,
        ListSorterNeedle,
        ChartGeneratorNeedle
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
