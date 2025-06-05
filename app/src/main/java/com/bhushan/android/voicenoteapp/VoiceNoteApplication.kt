package com.bhushan.android.voicenoteapp

import android.app.Application
import com.bhushan.android.voicenoteapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class VoiceNoteApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@VoiceNoteApplication)
            modules(appModule)
        }
    }
}