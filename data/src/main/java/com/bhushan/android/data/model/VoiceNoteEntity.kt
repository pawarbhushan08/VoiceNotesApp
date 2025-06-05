package com.bhushan.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bhushan.android.domain.model.VoiceNote

@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey val id: Long,
    val text: String,
    val timestamp: Long
) {
    fun asDomain(): VoiceNote = VoiceNote(id, text, timestamp)

    companion object {
        fun fromDomain(note: VoiceNote): VoiceNoteEntity =
            VoiceNoteEntity(note.id, note.text, note.timestamp)
    }
}