package com.plcoding.run.presentation.activerun.map

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.plcoding.core.domain.location.LocationTimestamp
import com.plcoding.core.domain.location.distanceTo
import kotlin.math.abs

object PolylineColorCalculator {

    private const val MIN_SPEED = 5.0
    private const val MAX_SPEED = 20.0

    fun locationsToColor(origin: LocationTimestamp, destination: LocationTimestamp): Color {
        val distanceMeters = origin.location.location.distanceTo(destination.location.location)
        val timeDiff = abs(
            (destination.durationTimestamp - origin.durationTimestamp).inWholeSeconds
        )
        val speedKmh = (distanceMeters / timeDiff) * 3.6
        return interpolateColor(
            speedKmh = speedKmh,
            minSpeed = MIN_SPEED,
            maxSpeed = MAX_SPEED,
            colorStart = Color.Green,
            colorMid = Color.Yellow,
            colorEnd = Color.Red
        )
    }

    private fun interpolateColor(
        speedKmh: Double,
        minSpeed: Double,
        maxSpeed: Double,
        colorStart: Color,
        colorMid: Color,
        colorEnd: Color
    ): Color {
        val ratio = ((speedKmh - minSpeed) / (maxSpeed - minSpeed))
            .coerceIn(0.0..1.0)
        val colorInt = if (ratio <= 0.5) {
            val midRatio = ratio / 0.5
            ColorUtils.blendARGB(colorStart.toArgb(), colorMid.toArgb(), midRatio.toFloat())
        } else {
            val midToEndRatio = (ratio - 0.5) / 0.5
            ColorUtils.blendARGB(colorMid.toArgb(), colorEnd.toArgb(), midToEndRatio.toFloat())
        }
        return Color(colorInt)
    }
}
