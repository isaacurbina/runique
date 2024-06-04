@file:OptIn(ExperimentalMaterial3Api::class)

package com.plcoding.run.presentation.activerun

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plcoding.auth.presentation.ObserveAsEvents
import com.plcoding.core.notification.ActiveRunService
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.core.presentation.designsystem.StartIcon
import com.plcoding.core.presentation.designsystem.StopIcon
import com.plcoding.core.presentation.designsystem.component.RuniqueActionButton
import com.plcoding.core.presentation.designsystem.component.RuniqueDialog
import com.plcoding.core.presentation.designsystem.component.RuniqueFloatingActionButton
import com.plcoding.core.presentation.designsystem.component.RuniqueOutlinedActionButton
import com.plcoding.core.presentation.designsystem.component.RuniqueScaffold
import com.plcoding.core.presentation.designsystem.component.RuniqueToolbar
import com.plcoding.run.presentation.R
import com.plcoding.run.presentation.activerun.component.RunDataCard
import com.plcoding.run.presentation.activerun.map.TrackerMap
import com.plcoding.run.presentation.util.hasLocationPermissions
import com.plcoding.run.presentation.util.hasNotificationPermission
import com.plcoding.run.presentation.util.requestRuniquePermissions
import com.plcoding.run.presentation.util.shouldShowLocationPermissionRationale
import com.plcoding.run.presentation.util.shouldShowNotificationPermissionRationale
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayOutputStream

@Composable
fun ActiveRunScreenRoot(
    viewModel: ActiveRunViewModel = koinViewModel(),
    onServiceToggle: (ActiveRunService.State) -> Unit,
    onBackClick: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is ActiveRunEvent.Error -> Toast.makeText(
                context,
                event.error.asString(context),
                Toast.LENGTH_LONG
            ).show()

            ActiveRunEvent.RunSaved -> onFinish()
        }
    }
    ActiveRunScreen(
        state = viewModel.state,
        onServiceToggle = onServiceToggle,
        onAction = {
            when (it) {
                ActiveRunAction.OnBackClick -> {
                    if (!viewModel.state.hasStartedRunning) {
                        onBackClick()
                    }
                }

                else -> Unit
            }
            viewModel.onAction(it)
        }
    )
}

@Composable
fun ActiveRunScreen(
    state: ActiveRunState,
    onServiceToggle: (ActiveRunService.State) -> Unit,
    onAction: (ActiveRunAction) -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val hasCourseLocationPermission = it[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val hasFineLocationPermission = it[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            it[Manifest.permission.POST_NOTIFICATIONS] == true
        } else true

        val activity = context as ComponentActivity
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()
        val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

        onAction(
            ActiveRunAction.SubmitLocationPermissionInfo(
                acceptedLocationPermission = hasCourseLocationPermission && hasFineLocationPermission,
                showLocationRationale = showLocationRationale
            )
        )

        onAction(
            ActiveRunAction.SubmitNotificationPermissionInfo(
                acceptedNotificationPermission = hasNotificationPermission,
                showNotificationRationale = showNotificationRationale
            )
        )
    }

    LaunchedEffect(key1 = true) {
        val activity = context as ComponentActivity
        val showLocationRationale = activity.shouldShowLocationPermissionRationale()
        val showNotificationRationale = activity.shouldShowNotificationPermissionRationale()

        onAction(
            ActiveRunAction.SubmitLocationPermissionInfo(
                acceptedLocationPermission = context.hasLocationPermissions(),
                showLocationRationale = showLocationRationale
            )
        )

        onAction(
            ActiveRunAction.SubmitNotificationPermissionInfo(
                acceptedNotificationPermission = context.hasNotificationPermission(),
                showNotificationRationale = showNotificationRationale
            )
        )

        if (!showLocationRationale && !showNotificationRationale) {
            permissionLauncher.requestRuniquePermissions(context)
        }
    }

    LaunchedEffect(key1 = state.isRunFinished) {
        if (state.isRunFinished) {
            onServiceToggle(ActiveRunService.State.STOPPED)
        }
    }

    val isServiceActive by ActiveRunService.isServiceActive.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = state.shouldTrack, key2 = isServiceActive) {
        if (context.hasLocationPermissions() && state.shouldTrack && !isServiceActive) {
            onServiceToggle(ActiveRunService.State.STARTED)
        }
    }

    RuniqueScaffold(
        withGradient = false,
        topAppBar = {
            RuniqueToolbar(
                showBackButton = true,
                title = stringResource(id = R.string.active_run),
                onBackClick = {
                    onAction(ActiveRunAction.OnBackClick)
                }
            )
        },
        floatingActionButton = {
            RuniqueFloatingActionButton(
                icon = if (state.shouldTrack) {
                    StopIcon
                } else StartIcon,
                onClick = {
                    onAction(ActiveRunAction.OnToggleRunClick)
                },
                iconSize = 20.dp,
                contentDescription = if (state.shouldTrack) {
                    stringResource(id = R.string.pause_run)
                } else stringResource(id = R.string.start_run)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            TrackerMap(
                isRunFinished = state.isRunFinished,
                currentLocation = state.currentLocation,
                locations = state.runData.locations,
                onSnapshot = { bitmap ->
                    val stream = ByteArrayOutputStream()
                    stream.use {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
                    }
                    onAction(ActiveRunAction.OnRunProcessed(stream.toByteArray()))
                },
                modifier = Modifier.fillMaxSize()
            )
            RunDataCard(
                elapsedTime = state.elapsedTime,
                runData = state.runData,
                modifier = Modifier
                    .padding(16.dp)
                    .padding(padding)
                    .fillMaxWidth()
            )
        }
    }

    if (!state.shouldTrack && state.hasStartedRunning) {
        RuniqueDialog(
            title = stringResource(id = R.string.running_is_paused),
            onDismiss = {
                onAction(ActiveRunAction.OnResumeRunClick)
            },
            description = stringResource(id = R.string.resume_or_finish_run),
            primaryButton = {
                RuniqueActionButton(
                    text = stringResource(id = R.string.resume),
                    isLoading = false,
                    onClick = {
                        onAction(ActiveRunAction.OnResumeRunClick)
                    },
                    modifier = Modifier.weight(1f)
                )
            },
            secondaryButton = {
                RuniqueOutlinedActionButton(
                    text = stringResource(id = R.string.finish),
                    isLoading = state.isSavingRun,
                    onClick = {
                        onAction(ActiveRunAction.OnFinishRunClick)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        )
    }

    if (state.showLocationRationale || state.showNotificationRationale) {
        RuniqueDialog(
            title = stringResource(id = R.string.permission_required),
            onDismiss = {
                /* Normal dismissing not allowed for permissions */
            },
            description = when {
                state.showLocationRationale && state.showNotificationRationale ->
                    stringResource(id = R.string.location_notification_rationale)

                state.showLocationRationale ->
                    stringResource(id = R.string.location_rationale)

                else -> stringResource(id = R.string.notification_rationale)
            },
            primaryButton = {
                RuniqueOutlinedActionButton(
                    text = stringResource(id = R.string.ok),
                    isLoading = false,
                    onClick = {
                        onAction(ActiveRunAction.DismissRationaleDialog)
                        permissionLauncher.requestRuniquePermissions(context)
                    }
                )
            })
    }
}

@Preview
@Composable
private fun ActiveRunScreenPreview() {
    RuniqueTheme {
        ActiveRunScreen(
            state = ActiveRunState(),
            onServiceToggle = {},
            onAction = {}
        )
    }
}
