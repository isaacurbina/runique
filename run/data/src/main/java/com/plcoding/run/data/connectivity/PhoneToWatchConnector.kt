package com.plcoding.run.data.connectivity

import com.plcoding.core.connectivity.domain.DeviceNode
import com.plcoding.core.connectivity.domain.DeviceType
import com.plcoding.core.connectivity.domain.NodeDiscovery
import com.plcoding.run.domain.WatchConnector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PhoneToWatchConnector(
    nodeDiscovery: NodeDiscovery,
    applicationScope: CoroutineScope
) : WatchConnector {

    private val _connectedNode = MutableStateFlow<DeviceNode?>(null)
    private val _isTrackable = MutableStateFlow(false)
    val messagingActions = nodeDiscovery
        .observeConnectedDevices(DeviceType.HANDHELD)
        .onEach { devices ->
            val node = devices.firstOrNull { it.isNearby }
            node?.let {
                _connectedNode.value = it
            }
        }
        .launchIn(applicationScope)

    override val connectedDevice: StateFlow<DeviceNode?> = _connectedNode.asStateFlow()

    override fun setIsTrackable(isTrackable: Boolean) {
        _isTrackable.value = isTrackable
    }
}
