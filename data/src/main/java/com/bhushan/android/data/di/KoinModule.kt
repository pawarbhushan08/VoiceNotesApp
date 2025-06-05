package com.bhushan.android.data.di

import androidx.room.Room
import com.bhushan.android.data.local.VoiceNoteDatabase
import com.bhushan.android.data.repository.VoiceNoteRepositoryImpl
import com.bhushan.android.data.services.TranscriptionServiceImpl
import com.bhushan.android.domain.repository.VoiceNoteRepository
import com.bhushan.android.domain.service.TranscriptionService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            VoiceNoteDatabase::class.java,
            "voice_note_db"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<VoiceNoteDatabase>().voiceNoteDao() }
    single<TranscriptionService> { TranscriptionServiceImpl(get()) }
    single<VoiceNoteRepository> { VoiceNoteRepositoryImpl(get()) }

}