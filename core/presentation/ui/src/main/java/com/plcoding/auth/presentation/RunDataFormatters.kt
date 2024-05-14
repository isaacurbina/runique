package com.plcoding.auth.presentation

import java.util.Locale
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.time.Duration

private const val DURATION_FORMATTED_FORMAT = "%02d"

fun Duration.formatted(): String {
    val totalSeconds = inWholeSeconds
    val hours = String.format(Locale.US, DURATION_FORMATTED_FORMAT, totalSeconds / (60 * 60))
    val minutes = String.format(Locale.US, DURATION_FORMATTED_FORMAT, (totalSeconds % 3600) / 60)
    val seconds = String.format(Locale.US, DURATION_FORMATTED_FORMAT, (totalSeconds % 60))

    return "$hours:$minutes:$seconds"
}

fun Double.toFormattedKm(): String {
    val result = this.roundToDecimals(1)
    return "$result km"
}

fun Duration.toFormattedPace(distanceKm: Double): String {
    if (this == Duration.ZERO || distanceKm <= 0.0) {
        return "-"
    }

    val secondsPerKm = (this.inWholeSeconds / distanceKm).roundToInt()
    val avgPaceMinutes = secondsPerKm / 60
    val avgPaceSeconds = String.format(Locale.US, DURATION_FORMATTED_FORMAT, secondsPerKm % 60)

    return "$avgPaceMinutes:$avgPaceSeconds / km"
}

private fun Double.roundToDecimals(decimalCount: Int): Double {
    val factor = 10f.pow(decimalCount)
    return round(this * factor) / factor
}
