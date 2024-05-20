package com.plcoding.run.presentation.runoverview

import com.plcoding.run.presentation.runoverview.model.RunUi

data class RunOverviewState(
    val runs: List<RunUi> = emptyList()
)
