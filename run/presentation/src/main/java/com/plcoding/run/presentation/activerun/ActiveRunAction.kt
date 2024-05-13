package com.plcoding.run.presentation.activerun

sealed interface ActiveRunAction {

    data object OnToggleRunClick : ActiveRunAction
    data object OnFinishRunClick : ActiveRunAction
    data object OnResumeRunClick : ActiveRunAction
    data object OnBackClick : ActiveRunAction
    data object DismissRationaleDialog : ActiveRunAction

    data class SubmitLocationPermissionInfo(
        val acceptedLocationPermission: Boolean,
        val showLocationRationale: Boolean
    ) : ActiveRunAction

    data class SubmitNotificationPermissionInfo(
        val acceptedNotificationPermission: Boolean,
        val showNotificationRationale: Boolean
    ) : ActiveRunAction
}
