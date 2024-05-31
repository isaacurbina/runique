@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.wear.run.domain

import com.plcoding.core.connectivity.domain.messaging.MessagingAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration

class RunningWearTracker(
    private val watchToPhoneConnector: PhoneConnector,
    private val exerciseTracker: ExerciseTracker,
    applicationScope: CoroutineScope
) {

    private val _heartRate = MutableStateFlow(0)
    val heartRate = _heartRate.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val _isTrackable = MutableStateFlow(false)
    val isTrackable = _isTrackable.asStateFlow()

    val distanceMeters = watchToPhoneConnector
        .messagingActions
        .filterIsInstance<MessagingAction.DistanceUpdate>()
        .map { it.distanceMeters }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Lazily,
            initialValue = 0
        )

    val elapsedTime = watchToPhoneConnector
        .messagingActions
        .filterIsInstance<MessagingAction.TimeUpdate>()
        .map { it.elapsedDuration }
        .stateIn(
            scope = applicationScope,
            started = SharingStarted.Lazily,
            initialValue = Duration.ZERO
        )

    init {
        watchToPhoneConnector
            .messagingActions
            .onEach { action ->
                when (action) {
                    MessagingAction.Trackable -> {
                        _isTrackable.value = true
                    }

                    MessagingAction.Untraceable ->
                        _isTrackable.value = false

                    else -> Unit
                }
            }
            .launchIn(applicationScope)

        watchToPhoneConnector
            .connectedNode
            .filterNotNull()
            .onEach {
                exerciseTracker.prepareExercise()
            }
            .launchIn(applicationScope)

        isTracking
            .flatMapLatest {
                if (it) {
                    exerciseTracker.heartRate
                } else emptyFlow()
            }
            .onEach {
                watchToPhoneConnector.sendActionToPhone(MessagingAction.HeartRateUpdate(it))
            }
            .launchIn(applicationScope)
    }

    fun setIsTracking(isTracking: Boolean) {
        _isTracking.value = isTracking
    }
}
