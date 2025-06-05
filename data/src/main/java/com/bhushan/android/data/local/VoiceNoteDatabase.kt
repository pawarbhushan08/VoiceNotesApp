package com.bhushan.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bhushan.android.data.model.VoiceNoteEntity

@Database(entities = [VoiceNoteEntity::class], version = 1)
abstract class VoiceNoteDatabase : RoomDatabase() {
    abstract fun voiceNoteDao(): VoiceNoteDao
}