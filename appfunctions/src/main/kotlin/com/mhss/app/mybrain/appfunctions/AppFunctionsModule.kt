package com.mhss.app.mybrain.appfunctions

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appFunctionsModule = module {
    singleOf(::MyBrainAppFunctions)
}
