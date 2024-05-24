package com.plcoding.runique

import androidx.annotation.StringRes

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = false,
    val showAnalyticsInstallDialog: Boolean = false,
    @StringRes val state: Int? = null
)
