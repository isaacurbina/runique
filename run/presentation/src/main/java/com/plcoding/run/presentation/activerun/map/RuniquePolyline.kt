package com.plcoding.run.presentation.activerun.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Polyline
import com.plcoding.core.domain.location.LocationTimestamp

@Composable
fun RuniquePolyline(
    locations: List<List<LocationTimestamp>>
) {
    val polylines = remember(locations) {
        locations.map {
            it.zipWithNext { origin, destination ->
                PolylineUi(
                    origin = origin.location.location,
                    destination = destination.location.location,
                    color = PolylineColorCalculator.locationsToColor(
                        origin = origin,
                        destination = destination
                    )
                )
            }
        }
    }

    polylines.forEach { polyline ->
        polyline.forEach {
            Polyline(
                points = listOf(
                    LatLng(it.origin.lat, it.origin.long),
                    LatLng(it.destination.lat, it.destination.long)
                ),
                color = it.color,
                jointType = JointType.BEVEL
            )
        }
    }
}
