package com.plcoding.run.presentation.di


import com.plcoding.run.presentation.activerun.ActiveRunViewModel
import com.plcoding.run.presentation.runoverview.RunOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val runViewModelModule = module {
    viewModelOf(::RunOverviewViewModel)
    viewModelOf(::ActiveRunViewModel)
}
