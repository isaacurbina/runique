package com.plcoding.run.presentation.activerun

import com.plcoding.auth.presentation.UiText

sealed interface ActiveRunEvent {
    data class Error(val error: UiText) : ActiveRunEvent
    data object RunSaved : ActiveRunEvent
}
