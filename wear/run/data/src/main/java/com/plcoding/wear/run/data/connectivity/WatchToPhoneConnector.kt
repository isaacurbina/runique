@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.wear.run.data.connectivity

import com.plcoding.core.connectivity.domain.DeviceNode
import com.plcoding.core.connectivity.domain.DeviceType
import com.plcoding.core.connectivity.domain.NodeDiscovery
import com.plcoding.core.connectivity.domain.messaging.MessagingAction
import com.plcoding.core.connectivity.domain.messaging.MessagingClient
import com.plcoding.core.connectivity.domain.messaging.MessagingError
import com.plcoding.core.domain.util.EmptyResult
import com.plcoding.wear.run.domain.PhoneConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn

class WatchToPhoneConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope,
    private val messagingClient: MessagingClient
) : PhoneConnector {

    private val _connectedNode = MutableStateFlow<DeviceNode?>(null)

    override val connectedNode: StateFlow<DeviceNode?> = _connectedNode.asStateFlow()

    override val messagingActions: Flow<MessagingAction> = nodeDiscovery
        .observeConnectedDevices(DeviceType.WEARABLE)
        .flatMapLatest { devices ->
            val node = devices.firstOrNull { it.isNearby }
            node?.let {
                _connectedNode.value = it
                messagingClient.connectToNode(it.id)
            } ?: emptyFlow()
        }
        .shareIn(
            scope = applicationScope,
            started = SharingStarted.Eagerly
        )

    override suspend fun sendActionToPhone(action: MessagingAction): EmptyResult<MessagingError> {
        return messagingClient.sendOrQueueAction(action)
    }
}
