@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.run.data.connectivity

import com.plcoding.core.connectivity.domain.DeviceNode
import com.plcoding.core.connectivity.domain.DeviceType
import com.plcoding.core.connectivity.domain.NodeDiscovery
import com.plcoding.core.connectivity.domain.messaging.MessagingAction
import com.plcoding.core.connectivity.domain.messaging.MessagingClient
import com.plcoding.core.connectivity.domain.messaging.MessagingError
import com.plcoding.core.domain.util.EmptyResult
import com.plcoding.run.domain.WatchConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

class PhoneToWatchConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope,
    private val messagingClient: MessagingClient
) : WatchConnector {

    private val _connectedNode = MutableStateFlow<DeviceNode?>(null)
    private val _isTrackable = MutableStateFlow(false)

    init {
        _connectedNode
            .filterNotNull()
            .flatMapLatest {
                _isTrackable
            }
            .onEach { isTrackable ->
                sendActionToWatch(MessagingAction.ConnectionRequest)
                val action = if (isTrackable) {
                    MessagingAction.Trackable
                } else MessagingAction.Untraceable
                sendActionToWatch(action)
            }
            .launchIn(applicationScope)
    }

    override val connectedDevice: StateFlow<DeviceNode?> = _connectedNode.asStateFlow()

    override fun setIsTrackable(isTrackable: Boolean) {
        _isTrackable.value = isTrackable
    }

    override val messagingActions: Flow<MessagingAction> = nodeDiscovery
        .observeConnectedDevices(DeviceType.HANDHELD)
        .flatMapLatest { devices ->
            val node = devices.firstOrNull { it.isNearby }
            node?.let {
                _connectedNode.value = it
                messagingClient.connectToNode(it.id)
            } ?: emptyFlow()
        }
        .onEach {
            if (it == MessagingAction.ConnectionRequest) {
                if (_isTrackable.value) {
                    sendActionToWatch(MessagingAction.Trackable)
                } else sendActionToWatch(MessagingAction.Untraceable)
            }
        }
        .shareIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly
        )

    override suspend fun sendActionToWatch(action: MessagingAction): EmptyResult<MessagingError> {
        return messagingClient.sendOrQueueAction(action)
    }
}
