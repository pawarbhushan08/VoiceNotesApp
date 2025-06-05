package com.bhushan.android.domain.model

data class VoiceNote(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val timestamp: Long
)