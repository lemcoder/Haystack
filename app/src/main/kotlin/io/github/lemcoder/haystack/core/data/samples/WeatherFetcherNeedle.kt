package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

object WeatherFetcherNeedle : SampleNeedle {
    override fun create() = Needle(
        id = UUID.randomUUID().toString(),
        name = "Weather Fetcher",
        description = "Fetches current weather information for a given city using wttr.in (no API key needed)",
        pythonCode = """
import requests

city_encoded = city.replace(" ", "+")
url = f"https://wttr.in/{city_encoded}?format=j1"

try:
    response = requests.get(url)
    data = response.json()
except Exception as e:
    print(f"Error fetching weather: {e}")
    raise SystemExit

# Extract current conditions
current = data.get("current_condition", [{}])[0]

temp = current.get("temp_C", "N/A")
humidity = current.get("humidity", "N/A")
condition = current.get("weatherDesc", [{"value": "N/A"}])[0]["value"]

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

