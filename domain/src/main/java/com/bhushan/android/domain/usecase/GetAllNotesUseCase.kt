package com.bhushan.android.domain.usecase

import com.bhushan.android.domain.model.VoiceNote
import com.bhushan.android.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase(
    private val repository: VoiceNoteRepository
) {
    operator fun invoke(): Flow<List<VoiceNote>> = repository.getAllNotes()
}