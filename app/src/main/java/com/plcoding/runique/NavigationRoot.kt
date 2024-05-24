package com.plcoding.runique

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.plcoding.auth.presentation.intro.IntroScreenRoot
import com.plcoding.auth.presentation.login.LoginScreenRoot
import com.plcoding.auth.presentation.register.RegisterScreenRoot
import com.plcoding.run.presentation.activerun.ActiveRunScreenRoot
import com.plcoding.run.presentation.activerun.service.ActiveRunService
import com.plcoding.run.presentation.runoverview.RunOverviewScreenRoot

@Composable
fun NavigationRoot(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onAnalyticsClick: () -> Unit
) {
    val startDestination = if (isLoggedIn) {
        RunGraphDestination.RUN.name
    } else AuthGraphDestination.AUTH.name
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authGraph(navController)
        runGraph(navController, onAnalyticsClick)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = AuthGraphDestination.INTRO.name,
        route = AuthGraphDestination.AUTH.name
    ) {
        composable(route = AuthGraphDestination.INTRO.name) {
            IntroScreenRoot(
                onSignedUpClick = {
                    navController.navigate(AuthGraphDestination.REGISTER.name)
                },
                onSignInClick = {
                    navController.navigate(AuthGraphDestination.LOGIN.name)
                }
            )
        }

        composable(route = AuthGraphDestination.REGISTER.name) {
            RegisterScreenRoot(
                onSignInClick = {
                    navController.navigate(AuthGraphDestination.LOGIN.name) {
                        popUpTo(route = AuthGraphDestination.REGISTER.name) {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                onSuccessfulRegistration = {
                    navController.navigate(AuthGraphDestination.LOGIN.name)
                }
            )
        }

        composable(route = AuthGraphDestination.LOGIN.name) {
            LoginScreenRoot(
                onLoginSuccess = {
                    navController.navigate(RunGraphDestination.RUN.name) {
                        popUpTo(AuthGraphDestination.AUTH.name) {
                            inclusive = true
                        }
                    }
                },
                onSignUpClick = {
                    navController.navigate(AuthGraphDestination.REGISTER.name) {
                        popUpTo(route = AuthGraphDestination.LOGIN.name) {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                }
            )
        }
    }
}

private fun NavGraphBuilder.runGraph(
    navController: NavHostController,
    onAnalyticsClick: () -> Unit
) {
    navigation(
        startDestination = RunGraphDestination.RUN_OVERVIEW.name,
        route = RunGraphDestination.RUN.name
    ) {
        composable(route = RunGraphDestination.RUN_OVERVIEW.name) {
            RunOverviewScreenRoot(
                onStartRunClick = {
                    navController.navigate(RunGraphDestination.ACTIVE_RUN.name)
                },
                onLogout = {
                    navController.navigate(AuthGraphDestination.AUTH.name) {
                        popUpTo(RunGraphDestination.RUN.name) {
                            inclusive = true
                        }
                    }
                },
                onAnalyticsClick = onAnalyticsClick
            )
        }
        composable(
            route = RunGraphDestination.ACTIVE_RUN.name,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "runique://active_run"
                }
            )
        ) {
            val context = LocalContext.current
            ActiveRunScreenRoot(
                onServiceToggle = { state ->
                    when (state) {
                        ActiveRunService.State.STARTED ->
                            context.startService(
                                ActiveRunService.createStartIntent(
                                    context = context,
                                    activityClass = MainActivity::class.java
                                )
                            )

                        ActiveRunService.State.STOPPED ->
                            context.startService(
                                ActiveRunService.createStopIntent(context = context)
                            )
                    }
                },
                onBackClick = {
                    navController.navigateUp()
                },
                onFinish = {
                    navController.navigateUp()
                }
            )
        }
    }
}
