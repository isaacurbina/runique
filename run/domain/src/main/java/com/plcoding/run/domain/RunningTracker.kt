@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.run.domain

import com.plcoding.core.connectivity.domain.messaging.MessagingAction
import com.plcoding.core.domain.LocationDataCalculator
import com.plcoding.core.domain.RuniqueTimer
import com.plcoding.core.domain.location.LocationTimestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RunningTracker(
    private val locationObserver: LocationObserver,
    applicationScope: CoroutineScope,
    private val watchConnector: WatchConnector
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

    private val heartRates = isTracking
        .flatMapLatest {
            if (it) {
                watchConnector.messagingActions
            } else emptyFlow()
        }
        .filterIsInstance<MessagingAction.HeartRateUpdate>()
        .map {
            it.heartRate
        }
        .runningFold(initial = emptyList<Int>()) { currentHeartRates, newHeartRate ->
            currentHeartRates + newHeartRate
        }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    init {
        isTracking
            .onEach { isTracking ->
                if (!isTracking) {
                    val newList = buildList {
                        addAll(runData.value.locations)
                        add(emptyList())
                        Unit
                    }.toList()
                    _runData.update {
                        it.copy(locations = newList)
                    }
                }
            }
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
            }.combine(heartRates) { location, heartRates ->
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
                        locations = newLocationsList,
                        heartRates = heartRates
                    )
                }
            }.launchIn(applicationScope)

        elapsedTime
            .onEach {
                watchConnector.sendActionToWatch(MessagingAction.TimeUpdate(it))
            }
            .launchIn(applicationScope)

        runData
            .map { it.distanceMeters }
            .distinctUntilChanged()
            .onEach {
                watchConnector.sendActionToWatch(MessagingAction.DistanceUpdate(it))
            }
            .launchIn(applicationScope)
    }

    fun startObservingLocation() {
        isObservingLocation.value = true
        watchConnector.setIsTrackable(true)
    }

    fun stopObservingLocation() {
        watchConnector.setIsTrackable(false)
        isObservingLocation.value = false
    }

    fun setIsTracking(tracking: Boolean) {
        _isTracking.value = tracking
    }

    fun finishRun() {
        stopObservingLocation()
        setIsTracking(false)
        _elapsedTime.value = Duration.ZERO
        _runData.value = RunData()
    }
}

private fun <T> List<List<T>>.replaceLast(replacement: List<T>): List<List<T>> {
    if (this.isEmpty()) {
        return listOf(replacement)
    }
    return this.dropLast(1) + listOf(replacement)
}
