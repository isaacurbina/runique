package com.plcoding.wear.run.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.core.connectivity.domain.messaging.MessagingAction
import com.plcoding.core.domain.util.Result
import com.plcoding.wear.run.domain.ExerciseTracker
import com.plcoding.wear.run.domain.PhoneConnector
import com.plcoding.wear.run.domain.RunningWearTracker
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
import kotlin.time.Duration

class TrackerViewModel(
    private val exerciseTracker: ExerciseTracker,
    private val phoneConnector: PhoneConnector,
    private val runningTracker: RunningWearTracker
) : ViewModel() {

    var state by mutableStateOf(TrackerState())
        private set

    private val hasBodySensorPermission = MutableStateFlow(false)

    private val eventChannel = Channel<TrackerEvent>()
    val events = eventChannel.receiveAsFlow()

    private val isTracking = snapshotFlow {
        state.isRunActive && state.isTrackable && state.isConnectedPhoneNearby
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = false
    )

    init {
        phoneConnector.connectedNode
            .filterNotNull()
            .onEach { phone ->
                state = state.copy(
                    isConnectedPhoneNearby = phone.isNearby
                )
            }
            .combine(isTracking) { _, isTracking ->
                if (!isTracking) {
                    phoneConnector.sendActionToPhone(MessagingAction.ConnectionRequest)
                }
            }
            .launchIn(viewModelScope)

        runningTracker
            .isTrackable
            .onEach { isTrackable ->
                state = state.copy(isTrackable = isTrackable)
            }
            .launchIn(viewModelScope)

        isTracking
            .onEach {
                val result = when {
                    it && !state.hasStartedRunning -> {
                        exerciseTracker.startExercise()
                    }

                    it && state.hasStartedRunning -> {
                        exerciseTracker.resumeExercise()
                    }

                    !it && state.hasStartedRunning -> {
                        exerciseTracker.pauseExercise()
                    }

                    else -> Result.Success(Unit)
                }
                if (result is Result.Error) {
                    result.error.toUiText()?.let {
                        eventChannel.send(TrackerEvent.Error(it))
                    }
                }
                if (it) {
                    state = state.copy(hasStartedRunning = true)
                }
                runningTracker.setIsTracking(it)
            }
            .launchIn(viewModelScope)

        verifyHeartRateTracking()

        runningTracker
            .heartRate
            .onEach {
                state = state.copy(heartRate = it)
            }
            .launchIn(viewModelScope)

        runningTracker
            .distanceMeters
            .onEach {
                state = state.copy(distanceMeters = it)
            }
            .launchIn(viewModelScope)

        runningTracker
            .elapsedTime
            .onEach {
                state = state.copy(elapsedDuration = it)
            }
            .launchIn(viewModelScope)

        listenToPhoneActions()
    }

    fun onAction(action: TrackerAction, triggeredOnPhone: Boolean = false) {
        if (!triggeredOnPhone) {
            sendActionToPhone(action)
        }
        when (action) {
            TrackerAction.OnFinishRunClick ->
                viewModelScope.launch {
                    exerciseTracker.stopExercise()
                    eventChannel.send(TrackerEvent.RunFinished)

                    state = state.copy(
                        elapsedDuration = Duration.ZERO,
                        distanceMeters = 0,
                        heartRate = 0,
                        hasStartedRunning = false,
                        isRunActive = false
                    )
                }

            TrackerAction.OnToggleRunClick -> {
                if (state.isTrackable) {
                    state = state.copy(
                        isRunActive = !state.isRunActive
                    )
                }
            }
            is TrackerAction.OnBodySensorPermissionResult -> {
                hasBodySensorPermission.value = action.isGranted
                if (action.isGranted) {
                    verifyHeartRateTracking()
                }
            }
        }
    }

    private fun verifyHeartRateTracking() = viewModelScope.launch {
        val isHeartRateTrackingSupported =
            exerciseTracker.isHeartRateTrackingSupported()
        state = state.copy(canTrackHeartRate = isHeartRateTrackingSupported)
    }

    private fun sendActionToPhone(action: TrackerAction) = viewModelScope.launch {
        val messagingAction = when (action) {
            is TrackerAction.OnBodySensorPermissionResult -> null
            TrackerAction.OnFinishRunClick -> MessagingAction.Finish
            TrackerAction.OnToggleRunClick ->
                if (state.isRunActive) {
                    MessagingAction.Pause
                } else MessagingAction.StartOrResume
        }
        messagingAction?.let {
            val result = phoneConnector.sendActionToPhone(it)
            if (result is Result.Error) {
                println("Tracker error: ${result.error}")
            }
        }
    }

    private fun listenToPhoneActions() {
        phoneConnector
            .messagingActions
            .onEach { action ->
                when (action) {
                    MessagingAction.Finish ->
                        onAction(TrackerAction.OnFinishRunClick, triggeredOnPhone = true)

                    MessagingAction.Pause ->
                        if (state.isTrackable) {
                            state = state.copy(isRunActive = false)
                        }

                    MessagingAction.StartOrResume ->
                        if (state.isTrackable) {
                            state = state.copy(isRunActive = true)
                        }

                    MessagingAction.Trackable -> {
                        state = state.copy(isTrackable = true)
                    }

                    MessagingAction.Untraceable -> {
                        state = state.copy(isTrackable = false)
                    }

                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }
}
