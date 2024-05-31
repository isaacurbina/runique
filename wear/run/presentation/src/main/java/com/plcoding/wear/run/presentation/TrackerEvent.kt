package com.plcoding.wear.run.presentation

import com.plcoding.auth.presentation.UiText

sealed interface TrackerEvent {
    data object RunFinished : TrackerEvent
    data class Error(val message: UiText) : TrackerEvent
}
