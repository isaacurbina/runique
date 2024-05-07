package com.plcoding.auth.presentation.login

import com.plcoding.auth.presentation.register.UiText

sealed interface LoginEvent {
    data class Error(val error: UiText) : LoginEvent
    data object LoginSuccess : LoginEvent
}
