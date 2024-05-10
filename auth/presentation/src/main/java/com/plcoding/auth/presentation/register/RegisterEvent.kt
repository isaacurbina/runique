package com.plcoding.auth.presentation.register

import com.plcoding.auth.presentation.UiText

sealed interface RegisterEvent {
    data object RegistrationSuccess : RegisterEvent
    data class Error(val error: UiText) : RegisterEvent
}
