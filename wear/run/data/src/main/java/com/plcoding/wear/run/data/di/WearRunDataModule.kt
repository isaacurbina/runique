package com.plcoding.wear.run.data.di

import com.plcoding.wear.run.data.HealthServicesExerciseTracker
import com.plcoding.wear.run.data.connectivity.WatchToPhoneConnector
import com.plcoding.wear.run.domain.ExerciseTracker
import com.plcoding.wear.run.domain.PhoneConnector
import com.plcoding.wear.run.domain.RunningWearTracker
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val wearRunDataModule = module {
    singleOf(::HealthServicesExerciseTracker).bind<ExerciseTracker>()
    singleOf(::WatchToPhoneConnector).bind<PhoneConnector>()
    singleOf(::RunningWearTracker)
    single {
        get<RunningWearTracker>().elapsedTime
    }
}
