package com.plcoding.wear.app.presentation

import android.app.Application
import com.plcoding.core.connectivity.data.di.coreConnectivityDataModule
import com.plcoding.wear.app.presentation.di.wearAppModule
import com.plcoding.wear.run.data.di.wearRunDataModule
import com.plcoding.wear.run.presentation.di.wearRunPresentationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RuniqueWearApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@RuniqueWearApp)
            modules(
                wearAppModule,
                wearRunPresentationModule,
                wearRunDataModule,
                coreConnectivityDataModule
            )
        }
    }
}
