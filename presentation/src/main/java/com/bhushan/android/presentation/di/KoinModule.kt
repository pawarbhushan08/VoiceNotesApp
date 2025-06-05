package com.bhushan.android.presentation.di

import com.bhushan.android.presentation.vm.VoiceNoteViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        VoiceNoteViewModel(
            startRecordingUseCase = get(),
            stopRecordingUseCase = get(),
            getAllNotesUseCase = get()
        )
    }
}