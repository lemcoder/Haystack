package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.core.model.needle.Needle
import io.github.lemcoder.core.model.needle.NeedleType
import java.util.UUID

object CryptoChartGeneratorNeedle : SampleNeedle {
  override fun create() =
    Needle(
      id = UUID.randomUUID().toString(),
      name = "Cryptocurrency Chart Generator",
      description =
        "Downloads today's price data for a given cryptocurrency and plots it using matplotlib.",
      pythonCode =
        """
        import matplotlib.pyplot as plt
        import os
        import requests
        from datetime import datetime, timedelta
        from com.chaquo.python import Python

        # Get a writable directory from Android
        context = Python.getPlatform().getApplication()
        files_dir = context.getFilesDir().getAbsolutePath()

        # CoinGecko market data URL (24h)
        url = f"https://api.coingecko.com/api/v3/coins/{crypto_name}/market_chart?vs_currency=usd&days=1"

        try:
            response = requests.get(url)
            response.raise_for_status()
            data = response.json()
        except Exception as e:
            print("ERROR: " + str(e))
            raise

        # Extract timestamps and prices
        prices = data.get("prices", [])

        if not prices:
            print("ERROR: No price data returned.")
            raise Exception("No price data.")

        timestamps = [p[0] / 1000 for p in prices]  # convert ms → sec
        price_values = [p[1] for p in prices]

        # Convert timestamps to hours/minutes for readability
        times_readable = [datetime.fromtimestamp(t).strftime("%H:%M") for t in timestamps]

        # Plot
        plt.figure(figsize=(10, 6))
        plt.plot(times_readable, price_values, marker='o', linewidth=2)
        plt.xticks(rotation=45)
        plt.title(f"{crypto_name.capitalize()} Price - Last 24 Hours")
        plt.xlabel("Time (HH:MM)")
        plt.ylabel("Price (USD)")
        plt.grid(True, alpha=0.3)
        plt.tight_layout()

        # Save image
        chart_path = os.path.join(files_dir, f"{crypto_name}_today_chart.png")
        plt.savefig(chart_path, dpi=120, bbox_inches='tight')
        plt.close()

        # Output the file path
        print(chart_path)
        """
          .trimIndent(),
      args =
        listOf(
          Needle.Arg(
            name = "crypto_name",
            description = "Name of the cryptocurrency (e.g., bitcoin, ethereum)",
            type = NeedleType.String,
          )
        ),
      returnType = NeedleType.Image,
    )
}
