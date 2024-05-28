package com.plcoding.wear.run.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedIconButton
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.plcoding.core.presentation.designsystem.PauseIcon
import com.plcoding.core.presentation.designsystem.StartIcon
import com.plcoding.designsystemwear.RuniqueWearTheme
import com.plcoding.wear.run.presentation.R

@Composable
fun ToggleRunButton(
    isRunActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedIconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        if (isRunActive) {
            Icon(
                imageVector = PauseIcon,
                contentDescription = stringResource(id = R.string.pause_run),
                tint = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Icon(
                imageVector = StartIcon,
                contentDescription = stringResource(id = R.string.start_run),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@WearPreviewDevices
@Composable
private fun ToggleRunButtonPreview() {
    RuniqueWearTheme {
        ToggleRunButton(
            isRunActive = true,
            onClick = { }
        )
    }
}
