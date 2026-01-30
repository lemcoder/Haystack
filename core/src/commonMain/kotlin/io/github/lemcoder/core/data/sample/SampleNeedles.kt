package io.github.lemcoder.core.data.sample

import io.github.lemcoder.core.model.needle.Needle
import kotlin.time.Clock

/**
 * Sample hardcoded needles for demonstration purposes. These needles showcase different
 * functionality and can be used for testing the needle execution system.
 */
object SampleNeedles {

    /**
     * Weather API needle that fetches current weather information for a given city. Uses the
     * wttr.in service which provides weather data without requiring an API key.
     */
    val weatherNeedle =
        Needle(
            id = "weather-api",
            name = "Get Weather",
            description =
                "Fetches current weather information for a specified city. Returns weather conditions, temperature, and other meteorological data.",
            code =
                """
                -- Fetch weather data from wttr.in (free weather API)
                -- Format: ?format=j1 returns JSON with detailed weather info
                local url = "https://wttr.in/" .. city .. "?format=j1"

                log:d("WeatherNeedle", "Fetching weather for: " .. city)

                local response = network:get(url)

                if response.status == 200 then
                    log:d("WeatherNeedle", "Weather data received successfully")
                    
                    -- The response body is already a JSON string
                    -- We can parse it or return it as-is
                    -- For simplicity, we'll extract key information and format it
                    
                    -- Return a formatted string with weather information
                    -- In a real implementation, you'd parse the JSON and extract specific fields
                    return response.body
                else
                    local errorMsg = "Failed to fetch weather data. Status: " .. tostring(response.status)
                    log:e("WeatherNeedle", errorMsg)
                    return errorMsg
                end
                """
                    .trimIndent(),
            args =
                listOf(
                    Needle.Arg(
                        name = "city",
                        type = Needle.Arg.Type.String,
                        description =
                            "The name of the city to get weather information for (e.g., 'London', 'Tokyo', 'New York')",
                        required = true,
                    )
                ),
            returnType = Needle.Arg.Type.String,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )

    /** Simple greeting needle that demonstrates parameter usage and string manipulation. */
    val greetingNeedle =
        Needle(
            id = "greeting",
            name = "Generate Greeting",
            description = "Generates a personalized greeting message for a given name.",
            code =
                """
                log:i("GreetingNeedle", "Generating greeting for: " .. name)
                return "Hello, " .. name .. "! Welcome to Haystack."
                """
                    .trimIndent(),
            args =
                listOf(
                    Needle.Arg(
                        name = "name",
                        type = Needle.Arg.Type.String,
                        description = "The name of the person to greet",
                        required = true,
                    )
                ),
            returnType = Needle.Arg.Type.String,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )

    /** Calculator needle that demonstrates numeric operations. */
    val calculatorNeedle =
        Needle(
            id = "calculator",
            name = "Calculate Sum",
            description = "Calculates the sum of two numbers.",
            code =
                """
                log:d("CalculatorNeedle", "Calculating: " .. tostring(a) .. " + " .. tostring(b))
                return a + b
                """
                    .trimIndent(),
            args =
                listOf(
                    Needle.Arg(
                        name = "a",
                        type = Needle.Arg.Type.Int,
                        description = "The first number",
                        required = true,
                    ),
                    Needle.Arg(
                        name = "b",
                        type = Needle.Arg.Type.Int,
                        description = "The second number",
                        required = true,
                    ),
                ),
            returnType = Needle.Arg.Type.Int,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
        )

    /** Get all sample needles as a list */
    fun getAll(): List<Needle> = listOf(weatherNeedle, greetingNeedle, calculatorNeedle)
}
