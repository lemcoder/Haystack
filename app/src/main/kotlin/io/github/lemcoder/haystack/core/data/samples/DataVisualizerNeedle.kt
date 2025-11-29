package io.github.lemcoder.haystack.core.data.samples

import io.github.lemcoder.haystack.core.model.needle.Needle
import io.github.lemcoder.haystack.core.model.needle.NeedleType
import java.util.UUID

object DataVisualizerNeedle : SampleNeedle {
    override fun create() = Needle(
        id = UUID.randomUUID().toString(),
        name = "Data Visualizer",
        description = "Creates a bar chart from comma-separated data values",
        pythonCode = """
import matplotlib.pyplot as plt
import os
from com.chaquo.python import Python

# Get a writable directory from the Android context
context = Python.getPlatform().getApplication()
files_dir = context.getFilesDir().getAbsolutePath()

# Parse the data
values = [float(x.strip()) for x in data.split(',')]
labels = [f"Item {i+1}" for i in range(len(values))]

# Create the bar chart
plt.figure(figsize=(10, 6))
bars = plt.bar(labels, values, color='steelblue', edgecolor='navy', linewidth=1.2)

# Customize the chart
plt.title(title, fontsize=16, fontweight='bold')
plt.xlabel('Categories', fontsize=12)
plt.ylabel('Values', fontsize=12)
plt.grid(axis='y', alpha=0.3, linestyle='--')

# Add value labels on bars
for bar in bars:
    height = bar.get_height()
    plt.text(bar.get_x() + bar.get_width()/2., height,
            f'{height:.1f}',
            ha='center', va='bottom', fontsize=10)

plt.tight_layout()

# Save the chart
chart_path = os.path.join(files_dir, "data_chart.png")
plt.savefig(chart_path, dpi=100, bbox_inches='tight')
plt.close()

print(f"Chart saved to: {chart_path}")
        """.trimIndent(),
        args = listOf(
            Needle.Arg(
                name = "data",
                type = NeedleType.String,
                description = "Comma-separated data values (e.g., '10.5, 20.3, 15.7, 30.2')",
                required = true
            ),
            Needle.Arg(
                name = "title",
                type = NeedleType.String,
                description = "Chart title",
                required = false,
                defaultValue = "\"Data Visualization\""
            )
        ),
        returnType = NeedleType.Image
    )
}
