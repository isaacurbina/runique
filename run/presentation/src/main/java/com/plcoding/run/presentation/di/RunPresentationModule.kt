package com.plcoding.run.presentation.di


import com.plcoding.run.presentation.runoverview.RunOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val runPresentationModule = module {
    viewModelOf(::RunOverviewViewModel)
}
