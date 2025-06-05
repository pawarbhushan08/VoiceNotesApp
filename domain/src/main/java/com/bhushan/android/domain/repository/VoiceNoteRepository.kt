package com.bhushan.android.domain.repository


import com.bhushan.android.domain.model.VoiceNote
import kotlinx.coroutines.flow.Flow

interface VoiceNoteRepository {
    suspend fun saveNote(note: VoiceNote)
    suspend fun getAllNotes(): Flow<List<VoiceNote>>
}