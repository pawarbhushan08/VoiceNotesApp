package com.bhushan.android.domain.usecase

import com.bhushan.android.domain.service.TranscriptionService
import kotlinx.coroutines.flow.Flow

class StartRecordingUseCase(private val transcriptionService: TranscriptionService) {
    operator fun invoke(): Flow<String> = transcriptionService.startListening()
}