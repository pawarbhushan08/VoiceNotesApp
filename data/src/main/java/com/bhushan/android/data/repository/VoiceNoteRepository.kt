package com.bhushan.android.data.repository

import com.bhushan.android.data.local.VoiceNoteDao
import com.bhushan.android.data.model.VoiceNoteEntity
import com.bhushan.android.domain.model.VoiceNote
import com.bhushan.android.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class VoiceNoteRepositoryImpl(
    private val dao: VoiceNoteDao
) : VoiceNoteRepository {
    override suspend fun saveNote(note: VoiceNote) {
        dao.insert(VoiceNoteEntity.fromDomain(note))
    }

    override fun getAllNotes(): Flow<List<VoiceNote>> =
        dao.getAllNotes().map { list -> list.map { it.asDomain() } }

    override fun searchNotes(query: String): Flow<List<VoiceNote>> =
        dao.searchNotes(query).map { list -> list.map { it.asDomain() } }
}