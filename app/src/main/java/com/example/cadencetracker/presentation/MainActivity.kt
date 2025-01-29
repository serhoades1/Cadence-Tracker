package com.example.cadencetracker.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.cadencetracker.presentation.theme.CadenceTrackerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var cadenceTracker: CadenceTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // Initialize sensor manager and tracker
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        cadenceTracker = CadenceTracker()

        setContent {
            var cadence by remember { mutableIntStateOf(0) }

            androidx.compose.runtime.LaunchedEffect(Unit) {
                while (true) {
                    delay(1000) // Update cadence every second
                    cadence = cadenceTracker.calculateCadence()
                }
            }

            WearApp(cadence)
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(cadenceTracker, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(cadenceTracker)
    }
}

class CadenceTracker : SensorEventListener {
    private val threshold = 10.0 // Sensitivity for step detection
    private val minStepInterval = 300 // Minimum interval between steps in ms
    private var lastTimestamp: Long = 0
    private var steps = 0
    private var startTime = System.currentTimeMillis()

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.values.size > 2) {
                val acceleration = it.values[2] // Z-axis acceleration
                if (acceleration > threshold) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTimestamp > minStepInterval) {
                        steps++
                        lastTimestamp = currentTime
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun calculateCadence(): Int {
        val elapsedTimeInSeconds = (System.currentTimeMillis() - startTime) / 1000
        return if (elapsedTimeInSeconds > 0) (steps / elapsedTimeInSeconds * 60).toInt() else 0
    }
}

@Composable
fun WearApp(cadence: Int) {
    CadenceTrackerTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            CadenceDisplay(cadence)
        }
    }
}

@Composable
fun CadenceDisplay(cadence: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Center the text within the box
    ) {
        Text(
            text = "$cadence steps/min",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(120) // Sample cadence value for preview
}
