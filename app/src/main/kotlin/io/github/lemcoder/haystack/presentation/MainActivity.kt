package io.github.lemcoder.haystack.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import io.github.lemcoder.haystack.navigation.Destination
import io.github.lemcoder.haystack.navigation.NavigationService
import io.github.lemcoder.koogedge.ui.util.SnackbarUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        setContent {
            MaterialTheme(
                colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            ) {
                MainScreen()
            }
        }
    }
}


@Composable
fun MainScreen() {
    val navigationService = remember { NavigationService.Instance }
    val destination by navigationService.destinationFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var chartBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(snackbarHostState) {
        SnackbarUtil.snackbarHostState = snackbarHostState
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        AnimatedContent(
            targetState = destination,
            modifier = Modifier.padding(innerPadding)
        ) { destination ->
            when (destination) {
                Destination.DownloadModel -> TODO()
                Destination.Home -> {
                    ChartScreen(
                        chartBitmap = chartBitmap,
                        onGenerateChart = {
                            coroutineScope.launch {
                                chartBitmap = generateChart()
                            }
                        }
                    )
                }

                Destination.Settings -> TODO()
                Destination.Needles -> TODO()
            }
        }
    }

    BackHandler {
        navigationService.navigateBack()
    }
}

@Composable
fun ChartScreen(
    chartBitmap: Bitmap?,
    onGenerateChart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onGenerateChart,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Chart")
        }

        chartBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Generated Chart",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } ?: run {
            Text(
                text = "No chart generated yet. Click the button to generate one.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

suspend fun generateChart(): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val py = Python.getInstance()

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

            // Get system modules
            val sys = py.getModule("sys")
            val io = py.getModule("io")
            val interpreter = py.getModule("interpreter")

            // Redirect stdout to capture output
            val textOutputStream = io.callAttr("StringIO")
            sys.put("stdout", textOutputStream)

            // Execute the Python code
            interpreter.callAttr("mainTextCode", pythonCode)

            // Get the output (chart path)
            val interpreterOutput = textOutputStream.callAttr("getvalue").toString().trim()
            Log.d("MainActivity", "Chart path: $interpreterOutput")

            // Decode the bitmap from the path
            val bitmap = BitmapFactory.decodeFile(interpreterOutput)

            if (bitmap == null) {
                Log.e("MainActivity", "Bitmap is null for path: $interpreterOutput")
            }

            bitmap
        } catch (e: Exception) {
            Log.e("MainActivity", "Error generating chart", e)
            null
        }
    }
}