package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

object WeatherFetcherNeedle : SampleNeedle {
    override fun create() = Needle(
        id = UUID.randomUUID().toString(),
        name = "Weather Fetcher",
        description = "Fetches current weather information for a given city (mock data for demo)",
        pythonCode = """
# Mock weather data for demonstration
import random

cities_weather = {
    "london": {"temp": 15, "condition": "Rainy", "humidity": 80},
    "paris": {"temp": 18, "condition": "Cloudy", "humidity": 65},
    "tokyo": {"temp": 22, "condition": "Sunny", "humidity": 55},
    "new york": {"temp": 20, "condition": "Partly Cloudy", "humidity": 60},
    "sydney": {"temp": 25, "condition": "Sunny", "humidity": 50}
}

city_lower = city.lower()

if city_lower in cities_weather:
    weather = cities_weather[city_lower]
    print(f"Weather in {city}:")
    print(f"Temperature: {weather['temp']}°C")
    print(f"Condition: {weather['condition']}")
    print(f"Humidity: {weather['humidity']}%")
else:
    # Generate random weather for unknown cities
    temp = random.randint(10, 30)
    conditions = ["Sunny", "Cloudy", "Rainy", "Partly Cloudy", "Stormy"]
    condition = random.choice(conditions)
    humidity = random.randint(40, 90)
    
    print(f"Weather in {city}:")
    print(f"Temperature: {temp}°C")
    print(f"Condition: {condition}")
    print(f"Humidity: {humidity}%")
        """.trimIndent(),
        args = listOf(
            Needle.Arg(
                name = "city",
                type = NeedleType.String,
                description = "City name to fetch weather for",
                required = true
            )
        ),
        returnType = NeedleType.String
    )
}
