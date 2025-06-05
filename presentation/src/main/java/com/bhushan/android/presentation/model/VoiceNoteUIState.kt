package com.bhushan.android.presentation.model

import com.bhushan.android.domain.model.VoiceNote

data class VoiceNoteUIState(
    val isRecording: Boolean = false,
    val isRecordingEditId: Long? = null,
    val currentTranscript: String = "",
    val recordingMillis: Long = 0L,
    val notes: List<VoiceNote> = emptyList(),
    val searchQuery: String = ""
)