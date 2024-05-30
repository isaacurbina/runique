package com.plcoding.wear.run.data.connectivity

import com.plcoding.core.connectivity.domain.DeviceNode
import com.plcoding.core.connectivity.domain.DeviceType
import com.plcoding.core.connectivity.domain.NodeDiscovery
import com.plcoding.wear.run.domain.PhoneConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WatchToPhoneConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope
) : PhoneConnector {

    private val _connectedNode = MutableStateFlow<DeviceNode?>(null)
    val messagingActions = nodeDiscovery
        .observeConnectedDevices(DeviceType.WEARABLE)
        .onEach { devices ->
            val node = devices.firstOrNull { it.isNearby }
            node?.let {
                _connectedNode.value = it
            }
        }
        .launchIn(applicationScope)

    override val connectedNode: StateFlow<DeviceNode?> = _connectedNode.asStateFlow()
}
