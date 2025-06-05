package com.bhushan.android.domain.usecase

import com.bhushan.android.domain.model.VoiceNote
import com.bhushan.android.domain.repository.VoiceNoteRepository

class StopRecordingUseCase(val repository: VoiceNoteRepository) {
    suspend operator fun invoke(transcript: String) {
        if (transcript.isNotBlank()) {
            repository.saveNote(
                VoiceNote(
                    text = transcript,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}