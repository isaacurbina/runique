package com.plcoding.runique

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.plcoding.core.domain.SessionStorage
import kotlinx.coroutines.launch

class MainViewModel(
    private val sessionStorage: SessionStorage
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        viewModelScope.launch {
            state = state.copy(isCheckingAuth = true)
            state = state.copy(
                isLoggedIn = sessionStorage.get() != null
            )
            state = state.copy(isCheckingAuth = false)
        }
    }

    fun handleInstallUpdate(
        @SplitInstallSessionStatus splitInstallSessionState: Int,
        onAction: (MainActivityAction) -> Unit
    ) {
        when (splitInstallSessionState) {
            SplitInstallSessionStatus.INSTALLED -> {
                println("INSTALLED")
                updateState(false, R.string.analytics_feature_was_installed)
            }

            SplitInstallSessionStatus.FAILED -> {
                println("FAILED")
                updateState(false, R.string.failed_to_install_analytics)
            }

            SplitInstallSessionStatus.INSTALLING -> {
                println("INSTALLING")
                updateState(true, R.string.installing_analytics)
            }

            SplitInstallSessionStatus.DOWNLOADING -> {
                println("DOWNLOADING")
                updateState(true, R.string.downloading_analytics)
            }

            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                println("REQUIRES_USER_CONFIRMATION")
                onAction(MainActivityAction.RequiresUserConfirmation)
            }

            SplitInstallSessionStatus.CANCELED -> {
                println("CANCELED")
                updateState(false, R.string.downloading_analytics)
            }

            SplitInstallSessionStatus.DOWNLOADED -> {
                println("DOWNLOADED")
                updateState(true, R.string.downloading_analytics)
            }

            SplitInstallSessionStatus.CANCELING -> {
                println("CANCELING")
                updateState(true, R.string.cancelling_installation)
            }

            SplitInstallSessionStatus.PENDING -> {
                println("PENDING")
                updateState(true, R.string.installation_pending)
            }

            SplitInstallSessionStatus.UNKNOWN -> {
                println("UNKNOWN")
                updateState(false, R.string.unknown_error)
            }
        }
    }

    fun updateState(isVisible: Boolean, @StringRes status: Int?) {
        state = state.copy(
            showAnalyticsInstallDialog = isVisible,
            state = status
        )
    }
}
