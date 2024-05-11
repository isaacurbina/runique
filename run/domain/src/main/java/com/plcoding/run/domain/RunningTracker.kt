@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.run.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class RunningTracker(
    private val locationObserver: LocationObserver
) {
    private val isObservingLocation = MutableStateFlow(false)

    val currentLocation = isObservingLocation
        .flatMapLatest {
            if (it) {
                locationObserver.observeLocation(1000L)
            } else emptyFlow()
        }.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Lazily,
            initialValue = null
        )

    fun startObservingLocation() {
        isObservingLocation.value = true
    }

    fun stopObservingLocation() {
        isObservingLocation.value = false
    }
}
