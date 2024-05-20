package com.plcoding.core.domain

import com.plcoding.core.domain.location.LocationTimestamp
import com.plcoding.core.domain.location.distanceTo
import kotlin.math.roundToInt
import kotlin.time.DurationUnit

object LocationDataCalculator {

    fun getTotalDistanceMeters(locations: List<List<LocationTimestamp>>): Int {
        return locations.sumOf { timestampsPerLine ->
            timestampsPerLine.zipWithNext { a, b ->
                a.location.location.distanceTo(b.location.location)
            }.sum().roundToInt()
        }
    }

    fun getMaxSpeedKmh(locations: List<List<LocationTimestamp>>): Double {
        return locations.maxOf { list ->
            list.zipWithNext { start, end ->
                val distance = start.location.location.distanceTo(end.location.location)
                val hoursDifference =
                    (end.durationTimestamp - start.durationTimestamp).toDouble(DurationUnit.HOURS)
                if (hoursDifference == 0.0) {
                    0.0
                } else (distance / 1000.0) / hoursDifference
            }.maxOrNull() ?: 0.0
        }
    }

    fun getTotalElevationMeters(locations: List<List<LocationTimestamp>>): Int {
        return locations.sumOf { list ->
            list.zipWithNext { start, end ->
                val altitudeStart = start.location.altitude
                val altitudeEnd = end.location.altitude
                (altitudeEnd - altitudeStart).coerceAtLeast(0.0)
            }.sum().roundToInt()
        }
    }
}
