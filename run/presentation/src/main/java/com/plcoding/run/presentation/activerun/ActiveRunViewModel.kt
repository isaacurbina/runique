package com.plcoding.run.presentation.activerun

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.run.domain.RunningTracker
import com.plcoding.run.presentation.activerun.service.ActiveRunService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class ActiveRunViewModel(
    private val runningTracker: RunningTracker
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
            ActiveRunAction.OnFinishRunClick -> {}
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

            else -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!ActiveRunService.isServiceActive) {
            runningTracker.stopObservingLocation()
        }
    }
}
