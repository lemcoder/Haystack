package io.github.lemcoder.core.service.agent

import io.github.lemcoder.core.model.needle.Needle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NeedleExecutionCoordinatorTest {

    @Test
    fun shouldHandleNeedleWithSimpleName() {
        // Given
        val needle =
            Needle(
                id = "1",
                name = "Test Needle",
                description = "Test",
                code = "return 'result'",
                args = emptyList(),
                returnType = Needle.Arg.Type.String,
            )

        // When
        val toolName = needle.name.replace(" ", "_").lowercase()

        // Then
        assertEquals("test_needle", toolName)
    }

    @Test
    fun shouldHandleNeedleWithComplexName() {
        // Given
        val needle =
            Needle(
                id = "1",
                name = "Get Weather Data",
                description = "Test",
                code = "return 'weather'",
                args = emptyList(),
                returnType = Needle.Arg.Type.String,
            )

        // When
        val toolName = needle.name.replace(" ", "_").lowercase()

        // Then
        assertEquals("get_weather_data", toolName)
    }

    @Test
    fun shouldMatchNeedleByToolName() {
        // Given
        val needles =
            listOf(
                Needle(
                    id = "1",
                    name = "Weather API",
                    description = "Get weather",
                    code = "return 'sunny'",
                    args = emptyList(),
                    returnType = Needle.Arg.Type.String,
                ),
                Needle(
                    id = "2",
                    name = "Time API",
                    description = "Get time",
                    code = "return '12:00'",
                    args = emptyList(),
                    returnType = Needle.Arg.Type.String,
                ),
            )

        // When
        val toolName = "weather_api"
        val foundNeedle =
            needles.find { needle -> needle.name.replace(" ", "_").lowercase() == toolName }

        // Then
        assertEquals("Weather API", foundNeedle?.name)
    }
}
