package io.github.lemcoder.haystack.presentation.screen.home

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.viewModelScope
import io.github.lemcoder.haystack.core.python.PythonExecutor
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.haystack.presentation.common.MviViewModel
import io.github.lemcoder.haystack.util.SnackbarUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val navigationService: NavigationService = NavigationService.Instance
) : MviViewModel<HomeState, HomeEvent>() {
    private val _state = MutableStateFlow(HomeState())
    override val state: StateFlow<HomeState> = _state.asStateFlow()

    override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.GenerateChart -> generateChart()
            HomeEvent.OpenSettings -> openSettings()
        }
    }

    private fun generateChart() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isGenerating = true,
                    errorMessage = null
                )

                val bitmap = generateChartInternal()

                _state.value = _state.value.copy(
                    chartBitmap = bitmap,
                    isGenerating = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGenerating = false,
                    errorMessage = "Error generating chart: ${e.message}"
                )
                SnackbarUtil.showSnackbar("Error generating chart: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private fun openSettings() {
        navigationService.navigateTo(Destination.Settings)
    }

    private suspend fun generateChartInternal(): Bitmap? {
        return withContext(Dispatchers.IO) {
            // Python code to generate chart
            val pythonCode = """
                import matplotlib.pyplot as plt
                import os
                from com.chaquo.python import Python
                
                # Get a writable directory from the Android context
                context = Python.getPlatform().getApplication()
                files_dir = context.getFilesDir().getAbsolutePath()
                
                # Generate the plot
                x = [1, 2, 3, 4, 5, 6, 7, 8, 9]
                y = [10, 20, 30, 26, 50, 34, 25, 66, 58]
                
                plt.figure(figsize=(6, 4))
                plt.plot(x, y, marker='o')
                plt.title('Simple Line Chart')
                plt.xlabel('X Axis')
                plt.ylabel('Y Axis')
                plt.grid(True)
                
                # Save the chart to the writable path
                chart_path = os.path.join(files_dir, "chart.png")
                plt.savefig(chart_path)
                plt.close()
                
                # Print the path so we can capture it
                print(chart_path)
            """.trimIndent()

            // Execute Python code and parse the result
            val result = PythonExecutor.executeAndParse(pythonCode) { output ->
                val chartPath = output.trim()
                Log.d("HomeViewModel", "Chart path: $chartPath")

                val bitmap = BitmapFactory.decodeFile(chartPath)
                if (bitmap == null) {
                    Log.e("HomeViewModel", "Bitmap is null for path: $chartPath")
                    throw IllegalStateException("Failed to decode bitmap from path: $chartPath")
                }
                bitmap
            }

            result.getOrElse { exception ->
                Log.e("HomeViewModel", "Error generating chart", exception)
                null
            }
        }
    }
}
