package com.plcoding.analytics.analyticsfeature

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.splitcompat.SplitCompat
import com.plcoding.analytics.data.di.analyticsDataModule
import com.plcoding.analytics.presentation.AnalyticsDashboardScreenRoot
import com.plcoding.analytics.presentation.di.analyticsPresentationModule
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import org.koin.core.context.GlobalContext.loadKoinModules

class AnalyticsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(
            listOf(
                analyticsDataModule,
                analyticsPresentationModule
            )
        )
        SplitCompat.installActivity(this)

        setContent {
            RuniqueTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = AnalyticsGraphDestination.ANALYTICS_DASHBOARD.name
                ) {
                    composable(route = AnalyticsGraphDestination.ANALYTICS_DASHBOARD.name) {
                        AnalyticsDashboardScreenRoot(
                            onBackClick = { finish() }
                        )
                    }
                }
            }
        }
    }
}
