package io.github.lemcoder.core.data.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DebugNeedleDataSourceTest {

    @Test
    fun shouldReturnSampleNeedles() = runTest {
        // Given
        val dataSource = DebugNeedleDataSource()

        // When
        val needles = dataSource.getNeedles()

        // Then
        assertTrue(needles.isNotEmpty(), "Should return sample needles")
        assertEquals(3, needles.size, "Should return 3 sample needles")
    }

    @Test
    fun shouldReturnWeatherNeedle() = runTest {
        // Given
        val dataSource = DebugNeedleDataSource()

        // When
        val needles = dataSource.getNeedles()

        // Then
        val weatherNeedle = needles.find { it.id == "weather-api" }
        assertNotNull(weatherNeedle, "Should include weather needle")
        assertEquals("Get Weather", weatherNeedle.name)
    }

    @Test
    fun shouldReturnGreetingNeedle() = runTest {
        // Given
        val dataSource = DebugNeedleDataSource()

        // When
        val needles = dataSource.getNeedles()

        // Then
        val greetingNeedle = needles.find { it.id == "greeting" }
        assertNotNull(greetingNeedle, "Should include greeting needle")
        assertEquals("Generate Greeting", greetingNeedle.name)
    }

    @Test
    fun shouldReturnCalculatorNeedle() = runTest {
        // Given
        val dataSource = DebugNeedleDataSource()

        // When
        val needles = dataSource.getNeedles()

        // Then
        val calcNeedle = needles.find { it.id == "calculator" }
        assertNotNull(calcNeedle, "Should include calculator needle")
        assertEquals("Calculate Sum", calcNeedle.name)
    }
}
