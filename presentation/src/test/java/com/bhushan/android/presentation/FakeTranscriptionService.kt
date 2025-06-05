package com.bhushan.android.presentation

import com.bhushan.android.domain.service.TranscriptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTranscriptionService : TranscriptionService {
    private var transcriptionText = ""
    private var shouldEmitText = true

    fun setTranscriptionText(text: String) {
        transcriptionText = text
    }

    fun setShouldEmitText(shouldEmit: Boolean) {
        shouldEmitText = shouldEmit
    }

    override fun startListening(): Flow<String> {
        return if (shouldEmitText) {
            flowOf(transcriptionText)
        } else {
            flowOf("")
        }
    }
}
