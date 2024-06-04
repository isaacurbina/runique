package com.plcoding.wear.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.plcoding.core.notification.ActiveRunService
import com.plcoding.designsystemwear.RuniqueWearTheme
import com.plcoding.wear.run.presentation.TrackerScreenRoot

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            RuniqueWearTheme {
                TrackerScreenRoot(
                    onServiceToggle = {
                        if (it) {
                            startService(
                                ActiveRunService.createStartIntent(
                                    context = this,
                                    activityClass = MainActivity::class.java
                                )
                            )
                        } else startService(
                            ActiveRunService.createStopIntent(context = this)
                        )
                    }
                )
            }
        }
    }
}
