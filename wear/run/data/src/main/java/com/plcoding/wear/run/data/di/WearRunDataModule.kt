package com.plcoding.wear.run.data.di

import com.plcoding.wear.run.data.HealthServicesExerciseTracker
import com.plcoding.wear.run.data.connectivity.WatchToPhoneConnector
import com.plcoding.wear.run.domain.ExerciseTracker
import com.plcoding.wear.run.domain.PhoneConnector
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val wearRunDataModule = module {
    singleOf(::HealthServicesExerciseTracker).bind<ExerciseTracker>()
    singleOf(::WatchToPhoneConnector).bind<PhoneConnector>()
}
