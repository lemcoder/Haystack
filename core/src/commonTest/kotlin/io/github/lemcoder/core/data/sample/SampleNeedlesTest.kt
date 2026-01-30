package io.github.lemcoder.core.data.sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SampleNeedlesTest {

    @Test
    fun shouldHaveWeatherNeedle() {
        // When
        val needle = SampleNeedles.weatherNeedle

        // Then
        assertEquals("weather-api", needle.id)
        assertEquals("Get Weather", needle.name)
        assertEquals(1, needle.args.size)
        assertEquals("city", needle.args.first().name)
        assertTrue(needle.code.contains("wttr.in"))
        assertTrue(needle.code.contains("network:get"))
    }

    @Test
    fun shouldHaveGreetingNeedle() {
        // When
        val needle = SampleNeedles.greetingNeedle

        // Then
        assertEquals("greeting", needle.id)
        assertEquals("Generate Greeting", needle.name)
        assertEquals(1, needle.args.size)
        assertEquals("name", needle.args.first().name)
        assertTrue(needle.code.contains("Hello"))
    }

    @Test
    fun shouldHaveCalculatorNeedle() {
        // When
        val needle = SampleNeedles.calculatorNeedle

        // Then
        assertEquals("calculator", needle.id)
        assertEquals("Calculate Sum", needle.name)
        assertEquals(2, needle.args.size)
        assertTrue(needle.code.contains("return a + b"))
    }

    @Test
    fun shouldReturnAllSampleNeedles() {
        // When
        val needles = SampleNeedles.getAll()

        // Then
        assertEquals(3, needles.size)
        assertNotNull(needles.find { it.id == "weather-api" })
        assertNotNull(needles.find { it.id == "greeting" })
        assertNotNull(needles.find { it.id == "calculator" })
    }

    @Test
    fun weatherNeedleShouldHaveCorrectReturnType() {
        // When
        val needle = SampleNeedles.weatherNeedle

        // Then
        assertTrue(needle.returnType is io.github.lemcoder.core.model.needle.Needle.Arg.Type.String)
    }

    @Test
    fun weatherNeedleShouldHaveCityArgument() {
        // When
        val needle = SampleNeedles.weatherNeedle
        val cityArg = needle.args.first()

        // Then
        assertEquals("city", cityArg.name)
        assertTrue(cityArg.type is io.github.lemcoder.core.model.needle.Needle.Arg.Type.String)
        assertTrue(cityArg.required)
        assertTrue(cityArg.description.isNotEmpty())
    }

    @Test
    fun weatherNeedleLuaCodeShouldBeValid() {
        // When
        val needle = SampleNeedles.weatherNeedle

        // Then
        // Verify the Lua code has expected structure
        assertTrue(needle.code.contains("local url"))
        assertTrue(needle.code.contains("log:d"))
        assertTrue(needle.code.contains("network:get(url)"))
        assertTrue(needle.code.contains("if response.status == 200"))
        assertTrue(needle.code.contains("return response.body"))
        assertTrue(needle.code.contains("log:e"))
    }

    @Test
    fun greetingNeedleShouldUseStringParameter() {
        // When
        val needle = SampleNeedles.greetingNeedle
        val nameArg = needle.args.first()

        // Then
        assertTrue(nameArg.type is io.github.lemcoder.core.model.needle.Needle.Arg.Type.String)
        assertTrue(nameArg.required)
    }

    @Test
    fun calculatorNeedleShouldHaveTwoIntParameters() {
        // When
        val needle = SampleNeedles.calculatorNeedle

        // Then
        assertEquals(2, needle.args.size)
        val aArg = needle.args.find { it.name == "a" }
        val bArg = needle.args.find { it.name == "b" }

        assertNotNull(aArg)
        assertNotNull(bArg)
        assertTrue(aArg.type is io.github.lemcoder.core.model.needle.Needle.Arg.Type.Int)
        assertTrue(bArg.type is io.github.lemcoder.core.model.needle.Needle.Arg.Type.Int)
    }
}
