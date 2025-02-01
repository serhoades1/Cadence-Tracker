package com.example.cadencetracker.presentation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

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
