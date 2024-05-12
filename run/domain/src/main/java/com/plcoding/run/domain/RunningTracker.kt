@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.run.domain

import com.plcoding.core.domain.LocationDataCalculator
import com.plcoding.core.domain.RuniqueTimer
import com.plcoding.core.domain.location.LocationTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RunningTracker(
    private val locationObserver: LocationObserver,
    private val applicationScope: CoroutineScope
) {
    private val _runData = MutableStateFlow(RunData())
    val runData = _runData.asStateFlow()

    private val _elapsedTime = MutableStateFlow(Duration.ZERO)
    val elapsedTime = _elapsedTime.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val isObservingLocation = MutableStateFlow(false)

    val currentLocation = isObservingLocation
        .flatMapLatest {
            if (it) {
                locationObserver.observeLocation(1000L)
            } else emptyFlow()
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Lazily,
            initialValue = null
        )

    init {
        isTracking
            .flatMapLatest {
                if (it) {
                    RuniqueTimer.timeAndEmit()
                } else emptyFlow()
            }.onEach {
                _elapsedTime.value += it
            }.launchIn(applicationScope)

        currentLocation
            .filterNotNull()
            .combineTransform(isTracking) { location, isTracking ->
                if (isTracking) {
                    emit(location)
                }
            }.zip(_elapsedTime) { location, elapsedTime ->
                LocationTimestamp(
                    location = location,
                    durationTimestamp = elapsedTime
                )
            }.onEach { location ->
                _runData.update {
                    val currentLocations = runData.value.locations
                    val lastLocationsList = if (currentLocations.isNotEmpty()) {
                        currentLocations.last() + location
                    } else listOf(location)
                    val newLocationsList = currentLocations.replaceLast(lastLocationsList)

                    val distanceMeters = LocationDataCalculator.getTotalDistanceMeters(
                        locations = newLocationsList
                    )
                    val distanceKm = distanceMeters / 1000.0
                    val currentDuration = location.durationTimestamp
                    val avgSecondsPerKm = if (distanceKm == 0.0) {
                        0
                    } else (currentDuration.inWholeSeconds / distanceKm).roundToInt()

                    RunData(
                        distanceMeters = distanceMeters,
                        pace = avgSecondsPerKm.seconds,
                        locations = newLocationsList
                    )
                }
            }.launchIn(applicationScope)
    }

    fun startObservingLocation() {
        isObservingLocation.value = true
    }

    fun stopObservingLocation() {
        isObservingLocation.value = false
    }

    fun setIsTracking(tracking: Boolean) {
        _isTracking.value = tracking
    }
}

private fun <T> List<List<T>>.replaceLast(replacement: List<T>): List<List<T>> {
    if (this.isEmpty()) {
        return listOf(replacement)
    }
    return this.dropLast(1) + listOf(replacement)
}
