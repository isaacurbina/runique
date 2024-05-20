package com.plcoding.run.presentation.runoverview

import com.plcoding.run.presentation.runoverview.model.RunUi

sealed interface RunOverviewAction {
    data object OnStartClick : RunOverviewAction
    data object OnLogoutClick : RunOverviewAction
    data object OnAnalyticsClick : RunOverviewAction
    data class DeleteRun(val runUi: RunUi) : RunOverviewAction
}
