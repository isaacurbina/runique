package com.plcoding.runique

sealed interface MainActivityAction {
    data object RequiresUserConfirmation : MainActivityAction
}