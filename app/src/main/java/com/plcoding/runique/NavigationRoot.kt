package com.plcoding.runique

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.plcoding.auth.presentation.intro.IntroScreenRoot
import com.plcoding.auth.presentation.intro.LoginScreenRoot
import com.plcoding.auth.presentation.register.RegisterScreenRoot

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

        composable(route = "register") {
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
                })
        }

        composable(route = "login") {
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
            Text(text = "Run overview!")
        }
    }
}
