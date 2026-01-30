package io.github.lemcoder.core.needle

import io.github.lemcoder.core.model.needle.Needle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NeedleParameterTest {

    @Test
    fun shouldCreateStringParameter() {
        // Given & When
        val param = NeedleParameter.StringParam("city", "London")

        // Then
        assertEquals("city", param.name)
        assertEquals("London", param.value)
        assertEquals(Needle.Arg.Type.String, param.type)
    }

    @Test
    fun shouldCreateIntParameter() {
        // Given & When
        val param = NeedleParameter.IntParam("count", 42)

        // Then
        assertEquals("count", param.name)
        assertEquals(42, param.value)
        assertEquals(Needle.Arg.Type.Int, param.type)
    }

    @Test
    fun shouldCreateFloatParameter() {
        // Given & When
        val param = NeedleParameter.FloatParam("price", 19.99f)

        // Then
        assertEquals("price", param.name)
        assertEquals(19.99f, param.value)
        assertEquals(Needle.Arg.Type.Float, param.type)
    }

    @Test
    fun shouldCreateBooleanParameter() {
        // Given & When
        val param = NeedleParameter.BooleanParam("enabled", true)

        // Then
        assertEquals("enabled", param.name)
        assertEquals(true, param.value)
        assertEquals(Needle.Arg.Type.Boolean, param.type)
    }

    @Test
    fun shouldGetValueFromStringParam() {
        // Given
        val param = NeedleParameter.StringParam("name", "John")

        // When
        val value = param.getValue()

        // Then
        assertTrue(value is String)
        assertEquals("John", value)
    }

    @Test
    fun shouldGetValueFromIntParam() {
        // Given
        val param = NeedleParameter.IntParam("age", 30)

        // When
        val value = param.getValue()

        // Then
        assertTrue(value is Int)
        assertEquals(30, value)
    }

    @Test
    fun shouldGetValueFromFloatParam() {
        // Given
        val param = NeedleParameter.FloatParam("temperature", 22.5f)

        // When
        val value = param.getValue()

        // Then
        assertTrue(value is Float)
        assertEquals(22.5f, value)
    }

    @Test
    fun shouldGetValueFromBooleanParam() {
        // Given
        val param = NeedleParameter.BooleanParam("active", false)

        // When
        val value = param.getValue()

        // Then
        assertTrue(value is Boolean)
        assertEquals(false, value)
    }

    @Test
    fun shouldConvertParametersToMap() {
        // Given
        val params =
            listOf(
                NeedleParameter.StringParam("name", "John"),
                NeedleParameter.IntParam("age", 30),
                NeedleParameter.FloatParam("height", 1.75f),
                NeedleParameter.BooleanParam("active", true),
            )

        // When
        val map = params.toParamMap()

        // Then
        assertEquals(4, map.size)
        assertEquals("John", map["name"])
        assertEquals(30, map["age"])
        assertEquals(1.75f, map["height"])
        assertEquals(true, map["active"])
    }

    @Test
    fun shouldConvertEmptyParametersToEmptyMap() {
        // Given
        val params = emptyList<NeedleParameter>()

        // When
        val map = params.toParamMap()

        // Then
        assertEquals(0, map.size)
    }

    @Test
    fun shouldConvertSingleParameterToMap() {
        // Given
        val params = listOf(NeedleParameter.StringParam("key", "value"))

        // When
        val map = params.toParamMap()

        // Then
        assertEquals(1, map.size)
        assertEquals("value", map["key"])
    }

    @Test
    fun shouldPreserveTypesWhenConvertingToMap() {
        // Given
        val params =
            listOf(
                NeedleParameter.IntParam("int_val", 123),
                NeedleParameter.FloatParam("float_val", 45.67f),
                NeedleParameter.BooleanParam("bool_val", true),
                NeedleParameter.StringParam("str_val", "text"),
            )

        // When
        val map = params.toParamMap()

        // Then
        assertTrue(map["int_val"] is Int)
        assertTrue(map["float_val"] is Float)
        assertTrue(map["bool_val"] is Boolean)
        assertTrue(map["str_val"] is String)
    }
}
