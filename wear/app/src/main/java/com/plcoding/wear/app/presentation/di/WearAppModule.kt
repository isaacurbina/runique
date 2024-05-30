package com.plcoding.wear.app.presentation.di

import com.plcoding.wear.app.presentation.RuniqueWearApp
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val wearAppModule = module {
    single {
        ((androidApplication()) as RuniqueWearApp).applicationScope
    }
}
