package com.example.cadencetracker.presentation

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay

class HapticFeedbackActivity : ComponentActivity() {

    private lateinit var cadenceTracker: CadenceTracker
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // Initialize cadence tracker separately
        cadenceTracker = CadenceTracker()

        // Initialize Vibrator
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        setContent {
            var cadence by remember { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000) // Update cadence every second
                    cadence = cadenceTracker.calculateCadence()
                    triggerHapticFeedback(cadence)
                }
            }

            HapticFeedbackUI(cadence)
        }
    }

    private fun triggerHapticFeedback(cadence: Int) {
        if (vibrator.hasVibrator()) {
            val amplitude = when {
                cadence < 100 -> 255  // High amplitude to encourage higher cadence
                cadence > 130 -> 50   // Low amplitude to encourage lower cadence
                else -> return vibrator.cancel() // No vibration, cadence in ideal range
            }

            val vibrationEffect = VibrationEffect.createOneShot(200, amplitude) // Same duration
            vibrator.vibrate(vibrationEffect)
        }
    }
}

@Composable
fun HapticFeedbackUI(cadence: Int) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cadence: $cadence steps/min",
                color = MaterialTheme.colors.primary
            )
        }
    }
}
