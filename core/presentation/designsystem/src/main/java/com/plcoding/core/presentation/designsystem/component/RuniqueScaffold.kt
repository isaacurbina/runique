package com.plcoding.core.presentation.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RuniqueScaffold(
    modifier: Modifier = Modifier,
    withGradient: Boolean = true,
    topAppBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topAppBar,
        floatingActionButton = floatingActionButton
    ) { padding ->
        if (withGradient) {
            GradientBackground {
                content(padding)
            }
        } else content(padding)

    }
}
