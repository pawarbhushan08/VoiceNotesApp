package com.bhushan.android.domain.di

import com.bhushan.android.domain.usecase.GetAllNotesUseCase
import com.bhushan.android.domain.usecase.StartRecordingUseCase
import com.bhushan.android.domain.usecase.StopRecordingUseCase
import org.koin.dsl.module

val domainModule = module {
    single { StartRecordingUseCase(get()) }
    single { StopRecordingUseCase(get()) }
    single { GetAllNotesUseCase(get()) }
}