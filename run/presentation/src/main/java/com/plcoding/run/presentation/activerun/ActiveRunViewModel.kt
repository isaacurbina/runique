package com.plcoding.run.presentation.activerun

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.auth.presentation.asUiText
import com.plcoding.core.domain.LocationDataCalculator
import com.plcoding.core.domain.location.Location
import com.plcoding.core.domain.run.Run
import com.plcoding.core.domain.run.RunRepository
import com.plcoding.core.domain.util.Result
import com.plcoding.run.domain.RunningTracker
import com.plcoding.run.domain.WatchConnector
import com.plcoding.run.presentation.activerun.service.ActiveRunService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.ZoneId
import java.time.ZonedDateTime

class ActiveRunViewModel(
    private val runningTracker: RunningTracker,
    private val runRepository: RunRepository,
    private val watchConnector: WatchConnector
) : ViewModel() {

    // region properties
    var state by mutableStateOf(
        ActiveRunState(
            shouldTrack = ActiveRunService.isServiceActive && runningTracker.isTracking.value
        )
    )
        private set

    private val eventChannel = Channel<ActiveRunEvent>()
    val events = eventChannel.receiveAsFlow()

    private val shouldTrack = snapshotFlow {
        state.shouldTrack
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = state.shouldTrack
    )

    private val hasLocationPermission = MutableStateFlow(false)

    private val isTracking =
        combine(shouldTrack, hasLocationPermission) { shouldTrack, hasPermission ->
            shouldTrack && hasPermission
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )
    // endregion

    init {
        watchConnector
            .connectedDevice
            .filterNotNull()
            .onEach {
                Timber.d("New device detected: ${it.displayName}")
            }
        hasLocationPermission.onEach {
            if (it) {
                runningTracker.startObservingLocation()
            } else runningTracker.stopObservingLocation()
        }.launchIn(viewModelScope)

        isTracking.onEach {
            runningTracker.setIsTracking(it)
        }.launchIn(viewModelScope)

        runningTracker.currentLocation.onEach {
            state = state.copy(currentLocation = it?.location)
        }.launchIn(viewModelScope)

        runningTracker.runData.onEach {
            state = state.copy(runData = it)
        }.launchIn(viewModelScope)

        runningTracker.elapsedTime.onEach {
            state = state.copy(elapsedTime = it)
        }.launchIn(viewModelScope)
    }

    fun onAction(action: ActiveRunAction) {
        when (action) {
            ActiveRunAction.OnFinishRunClick -> {
                state = state.copy(
                    isRunFinished = true,
                    isSavingRun = true
                )
            }

            ActiveRunAction.OnResumeRunClick -> {
                state = state.copy(
                    shouldTrack = true
                )
            }

            ActiveRunAction.OnToggleRunClick -> {
                state = state.copy(
                    hasStartedRunning = true,
                    shouldTrack = !state.shouldTrack
                )
            }

            ActiveRunAction.DismissRationaleDialog -> {
                state = state.copy(
                    showNotificationRationale = false,
                    showLocationRationale = false
                )
            }

            ActiveRunAction.OnBackClick -> {
                state = state.copy(
                    shouldTrack = false
                )
            }

            is ActiveRunAction.SubmitLocationPermissionInfo -> {
                hasLocationPermission.value = action.acceptedLocationPermission
                state = state.copy(
                    showLocationRationale = action.showLocationRationale
                )
            }

            is ActiveRunAction.SubmitNotificationPermissionInfo -> {
                state = state.copy(
                    showNotificationRationale = action.showNotificationRationale
                )
            }

            is ActiveRunAction.OnRunProcessed -> {
                finishRun(action.mapPictureBytes)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!ActiveRunService.isServiceActive) {
            runningTracker.stopObservingLocation()
        }
    }

    private fun finishRun(mapPicturesBytes: ByteArray) {
        val locations = state.runData.locations
        if (locations.isEmpty() || locations.first().size <= 1) {
            state = state.copy(isSavingRun = false)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val run = Run(
                id = null,
                duration = state.elapsedTime,
                dateTimeUtc = ZonedDateTime.now()
                    .withZoneSameInstant(ZoneId.of("UTC")),
                distanceMeters = state.runData.distanceMeters,
                location = state.currentLocation ?: Location(0.0, 0.0),
                maxSpeedKmh = LocationDataCalculator.getMaxSpeedKmh(locations),
                totalElevationMeters = LocationDataCalculator.getTotalElevationMeters(locations),
                mapPictureUrl = null
            )

            runningTracker.finishRun()

            when (val result = runRepository.upsertRun(run, mapPicturesBytes)) {
                is Result.Error -> eventChannel.send(ActiveRunEvent.Error(result.error.asUiText()))
                is Result.Success -> eventChannel.send(ActiveRunEvent.RunSaved)
            }
            state = state.copy(isSavingRun = false)
        }
    }
}
