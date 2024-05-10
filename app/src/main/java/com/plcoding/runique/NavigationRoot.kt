package com.plcoding.runique

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.plcoding.auth.presentation.intro.IntroScreenRoot
import com.plcoding.auth.presentation.login.LoginScreenRoot
import com.plcoding.auth.presentation.register.RegisterScreenRoot
import com.plcoding.run.presentation.activerun.ActiveRunScreenRoot
import com.plcoding.run.presentation.runoverview.RunOverviewScreenRoot

@Composable
fun NavigationRoot(
    navController: NavHostController,
    isLoggedIn: Boolean
) {
    val startDestination = if (isLoggedIn) {
        RunGraphDestination.RUN.name
    } else AuthGraphDestination.AUTH.name
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        authGraph(navController)
        runGraph(navController)
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
                    // TODO(Isaac) - this navigation is not working, this code is not executing
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

private fun NavGraphBuilder.runGraph(navController: NavHostController) {
    navigation(
        startDestination = RunGraphDestination.RUN_OVERVIEW.name,
        route = RunGraphDestination.RUN.name
    ) {
        composable(route = RunGraphDestination.RUN_OVERVIEW.name) {
            RunOverviewScreenRoot(
                onStartRunClick = {
                    navController.navigate(RunGraphDestination.ACTIVE_RUN.name)
                }
            )
        }
        composable(route = RunGraphDestination.ACTIVE_RUN.name) {
            ActiveRunScreenRoot()
        }
    }
}
