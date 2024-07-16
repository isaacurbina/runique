package com.plcoding.test

import com.plcoding.core.connectivity.domain.DeviceNode
import com.plcoding.core.connectivity.domain.messaging.MessagingAction
import com.plcoding.core.connectivity.domain.messaging.MessagingError
import com.plcoding.core.domain.util.EmptyResult
import com.plcoding.core.domain.util.Result
import com.plcoding.run.domain.WatchConnector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class PhoneToWatchConnectorFake : WatchConnector {

    private var sendError: MessagingError? = null
    private val _connectedDevice = MutableStateFlow<DeviceNode?>(null)
    private val _messagingActions = MutableSharedFlow<MessagingAction>()
    private val _isTrackable = MutableStateFlow(true)

    override val connectedDevice: StateFlow<DeviceNode?> =
        _connectedDevice.asStateFlow()

    override val messagingActions: Flow<MessagingAction> =
        _messagingActions.asSharedFlow()

    override suspend fun sendActionToWatch(action: MessagingAction): EmptyResult<MessagingError> {
        return sendError?.let {
            Result.Error(it)
        } ?: Result.Success(Unit)
    }

    override fun setIsTrackable(isTrackable: Boolean) {
        _isTrackable.value = isTrackable
    }

    suspend fun sendFromWatchToPhone(action: MessagingAction) {
        _messagingActions.emit(action)
    }
}
