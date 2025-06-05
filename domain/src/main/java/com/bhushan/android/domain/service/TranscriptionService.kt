package com.bhushan.android.domain.service

import kotlinx.coroutines.flow.Flow

interface TranscriptionService {
    fun startListening(): Flow<String>
}