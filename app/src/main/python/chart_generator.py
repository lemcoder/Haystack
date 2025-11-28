import matplotlib.pyplot as plt
import os
from com.chaquo.python import Python

def create_chart():
    # --- 1. Get a writable directory from the Android context ---
    # This is the crucial step to prevent the read-only file system error.
    context = Python.getPlatform().getApplication()
    files_dir = context.getFilesDir().getAbsolutePath()

    # --- 2. Generate the plot as before ---
    x = [1, 2, 3, 4, 5, 6,7,8,9]
    y = [10, 20, 30, 26, 50, 34, 25, 66, 58]

    plt.figure(figsize=(6, 4))
    plt.plot(x, y, marker='o')
    plt.title('Simple Line Chart')
    plt.xlabel('X Axis')
    plt.ylabel('Y Axis')
    plt.grid(True)

    # --- 3. Save the chart to the correct, writable path ---
    # Create a full path by joining the directory and the filename.
    chart_path = os.path.join(files_dir, "chart.png")
    plt.savefig(chart_path)
    plt.close() # It's good practice to close the plot to free up memory.

    # --- 4. Return the full path to the saved chart ---
    # The Android/Kotlin/Java code will need this full path to load the image.
    return chart_path