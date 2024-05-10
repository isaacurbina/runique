@file:Suppress("OPT_IN_USAGE_FUTURE_ERROR")
@file:OptIn(ExperimentalFoundationApi::class)

package com.plcoding.auth.presentation.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.auth.domain.AuthRepository
import com.plcoding.auth.domain.UserDataValidator
import com.plcoding.auth.presentation.R
import com.plcoding.auth.presentation.register.UiText
import com.plcoding.auth.presentation.register.asUiText
import com.plcoding.core.domain.util.DataError
import com.plcoding.core.domain.util.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userDataValidator: UserDataValidator
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    private val eventChannel = Channel<LoginEvent>()
    val events = eventChannel.receiveAsFlow()

    init {
        combine(state.email.textAsFlow(), state.password.textAsFlow()) { email, password ->
            state = state.copy(
                canLogin = userDataValidator.isValidEmail(email = email.toString().trim()) &&
                        password.toString().isNotBlank()
            )
        }.launchIn(viewModelScope)
    }

    fun onAction(action: LoginAction) {
        when (action) {
            LoginAction.OnRegisterClick -> Unit
            LoginAction.OnTogglePasswordVisibility -> {
                state = state.copy(
                    isPasswordVisible = !state.isPasswordVisible
                )
            }

            LoginAction.OnLoginClick -> login()
        }
    }

    private fun login() = viewModelScope.launch {
        state = state.copy(isLoggingIn = true)
        val result = authRepository.login(
            email = state.email.text.toString().trim(),
            password = state.password.text.toString()
        )
        state = state.copy(isLoggingIn = false)
        val event = when (result) {
            is Result.Success -> {
                LoginEvent.LoginSuccess
            }

            is Result.Error -> {
                val event = if (result.error == DataError.Network.UNAUTHORIZED) {
                    UiText.StringResource(R.string.error_email_password_incorrect)
                } else result.error.asUiText()
                LoginEvent.Error(event)
            }
        }
        eventChannel.send(event)
    }
}
