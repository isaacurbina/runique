package com.plcoding.run.presentation.activerun.map

import androidx.compose.ui.graphics.Color
import com.plcoding.core.domain.location.Location

data class PolylineUi(
    val origin: Location,
    val destination: Location,
    val color: Color
)
