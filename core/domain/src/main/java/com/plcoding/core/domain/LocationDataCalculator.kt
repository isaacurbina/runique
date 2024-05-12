package com.plcoding.core.domain

import com.plcoding.core.domain.location.LocationTimestamp
import com.plcoding.core.domain.location.distanceTo
import kotlin.math.roundToInt

object LocationDataCalculator {

    fun getTotalDistanceMeters(locations: List<List<LocationTimestamp>>): Int {
        return locations.sumOf { timestampsPerLine ->
            timestampsPerLine.zipWithNext { a, b ->
                a.location.location.distanceTo(b.location.location)
            }.sum().roundToInt()
        }
    }
}
