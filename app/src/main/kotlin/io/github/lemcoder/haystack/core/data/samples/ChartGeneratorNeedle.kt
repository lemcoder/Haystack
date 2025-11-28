package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

object ChartGeneratorNeedle : SampleNeedle {
    override fun create() = Needle(
        id = UUID.randomUUID().toString(),
        name = "Chart Generator",
        description = "Generates a simple line chart using matplotlib",
        pythonCode = """
import matplotlib.pyplot as plt
import os
from com.chaquo.python import Python

# Get a writable directory from the Android context
context = Python.getPlatform().getApplication()
files_dir = context.getFilesDir().getAbsolutePath()

# Sample data
x_values = [1, 2, 3, 4, 5]
y_values = [10, 20, 15, 30, 25]

# Generate the plot
plt.figure(figsize=(8, 6))
plt.plot(x_values, y_values, marker='o', linewidth=2)
plt.title('Sample Chart')
plt.xlabel('X Axis')
plt.ylabel('Y Axis')
plt.grid(True, alpha=0.3)

# Save the chart to the writable path
chart_path = os.path.join(files_dir, "generated_chart.png")
plt.savefig(chart_path, dpi=100, bbox_inches='tight')
plt.close()

# Print the path so we can capture it
print(chart_path)
        """.trimIndent(),
        args = emptyList(),
        returnType = NeedleType.Image
    )
}
