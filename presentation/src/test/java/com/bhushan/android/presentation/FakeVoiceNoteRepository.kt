package com.bhushan.android.presentation

import com.bhushan.android.domain.model.VoiceNote
import com.bhushan.android.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Test doubles (fake implementations)
class FakeVoiceNoteRepository : VoiceNoteRepository {
    private val notes = mutableListOf<VoiceNote>()
    private var shouldThrowError = false
    var saveNoteCalled = false
    var lastSavedNote: VoiceNote? = null

    fun setShouldThrowError(shouldThrow: Boolean) {
        shouldThrowError = shouldThrow
    }

    fun addNote(note: VoiceNote) {
        notes.add(note)
    }

    fun clear() {
        notes.clear()
        saveNoteCalled = false
        lastSavedNote = null
    }

    override suspend fun getAllNotes(): Flow<List<VoiceNote>> {
        if (shouldThrowError) {
            throw RuntimeException("Database error")
        }
        return flowOf(notes.toList())
    }

    override suspend fun saveNote(note: VoiceNote) {
        saveNoteCalled = true
        lastSavedNote = note
        val existingIndex = notes.indexOfFirst { it.id == note.id }
        if (existingIndex >= 0) {
            notes[existingIndex] = note
        } else {
            val newNote = if (note.id == 0L) {
                note.copy(id = (notes.maxOfOrNull { it.id } ?: 0) + 1)
            } else note
            notes.add(newNote)
        }
    }
}