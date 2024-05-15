package com.plcoding.run.presentation.runoverview.mapper

import android.os.Build
import com.plcoding.auth.presentation.formatted
import com.plcoding.auth.presentation.toFormattedKm
import com.plcoding.auth.presentation.toFormattedKmh
import com.plcoding.auth.presentation.toFormattedMeters
import com.plcoding.auth.presentation.toFormattedPace
import com.plcoding.core.domain.run.Run
import com.plcoding.run.presentation.runoverview.model.RunUi
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Run.toRunUi(): RunUi {
    val dateTimeInLocalTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        dateTimeUtc.withZoneSameInstant(ZoneId.systemDefault())
    } else {
        // TODO(Research how to do this in Android 0<
        dateTimeUtc
    }
    val formattedDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        DateTimeFormatter
            .ofPattern("MMM dd, yyyy - hh:mma")
            .format(dateTimeInLocalTime)
    } else {
        // TODO(Research how to do this in Android 0<
        dateTimeInLocalTime.toString()
    }
    val distanceKm = distanceMeters / 1000.0

    return RunUi(
        id = id.orEmpty(),
        duration = duration.formatted(),
        dateTime = formattedDateTime,
        distance = distanceKm.toFormattedKm(),
        avgSpeed = avgSpeedKmh.toFormattedKmh(),
        maxSpeed = maxSpeedKmh.toFormattedKmh(),
        pace = duration.toFormattedPace(distanceKm),
        totalElevation = totalElevationMeters.toFormattedMeters(),
        mapPictureUrl = mapPictureUrl
    )
}
