package com.plcoding.auth.presentation.login

sealed interface LoginAction {
    data object OnTogglePasswordVisibility : LoginAction
    data object onLoginClick : LoginAction
    data object OnRegisterClick : LoginAction
}
