package com.bhushan.android.voicenoteapp.di

import com.bhushan.android.data.di.dataModule
import com.bhushan.android.domain.di.domainModule
import com.bhushan.android.presentation.di.presentationModule

val appModule = listOf(presentationModule, domainModule, dataModule)