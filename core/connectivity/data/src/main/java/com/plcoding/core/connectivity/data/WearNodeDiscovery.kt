package com.plcoding.core.connectivity.data

import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import com.plcoding.core.connectivity.domain.DeviceNode
import com.plcoding.core.connectivity.domain.DeviceType
import com.plcoding.core.connectivity.domain.NodeDiscovery
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WearNodeDiscovery(
    context: Context
) : NodeDiscovery {

    private val capabilityClient = Wearable.getCapabilityClient(context)

    override fun observeConnectedDevices(
        localDeviceType: DeviceType
    ): Flow<Set<DeviceNode>> {
        return callbackFlow {
            val remoteCapability = when (localDeviceType) {
                DeviceType.WEARABLE -> REMOTE_CAPABILITY_WEAR
                DeviceType.HANDHELD -> REMOTE_CAPABILITY_PHONE
            }
            try {
                val capability = capabilityClient
                    .getCapability(remoteCapability, CapabilityClient.FILTER_REACHABLE)
                    .await()
                val connectedDevices = capability.nodes.map { it.toDeviceNode() }.toSet()
                send(connectedDevices)
            } catch (e: ApiException) {
                awaitClose()
                return@callbackFlow
            }
            val listener: (CapabilityInfo) -> Unit = { info ->
                trySend(info.nodes.map { it.toDeviceNode() }.toSet())
            }
            capabilityClient.addListener(listener, remoteCapability)
            awaitClose {
                capabilityClient.removeListener(listener)
            }
        }
    }

    companion object {
        private const val REMOTE_CAPABILITY_WEAR = "runique_wear_capability"
        private const val REMOTE_CAPABILITY_PHONE = "runique_phone_capability"
    }
}
