package com.bhushan.android.presentation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhushan.android.domain.usecase.GetAllNotesUseCase
import com.bhushan.android.domain.usecase.StartRecordingUseCase
import com.bhushan.android.domain.usecase.StopRecordingUseCase
import com.bhushan.android.presentation.model.VoiceNoteUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class VoiceNoteEvent {
    data class ShowSnackbar(val message: String) : VoiceNoteEvent()
}

class VoiceNoteViewModel(
    val startRecordingUseCase: StartRecordingUseCase,
    val stopRecordingUseCase: StopRecordingUseCase,
    private val getAllNotesUseCase: GetAllNotesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceNoteUIState())
    val uiState: StateFlow<VoiceNoteUIState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VoiceNoteEvent>()
    val events: SharedFlow<VoiceNoteEvent> = _events.asSharedFlow()

    private var transcriptJob: Job? = null

    init {
        loadAllNotes()
    }

    private fun loadAllNotes() {
        viewModelScope.launch {
            getAllNotesUseCase().collect { noteList ->
                _uiState.update { it.copy(notes = noteList) }
            }
        }
    }

    fun onRecordClicked() {
        if (_uiState.value.isRecording || _uiState.value.isRecordingEditId != null) return
        transcriptJob?.cancel()
        _uiState.update {
            it.copy(
                isRecording = true,
                currentTranscript = "",
                recordingMillis = 0L
            )
        }
        transcriptJob = viewModelScope.launch {
            _events.emit(VoiceNoteEvent.ShowSnackbar("Recording started"))
            val startTime = System.currentTimeMillis()
            startRecordingUseCase().collect { text ->
                // Even if nothing is said, collect might emit empty string
                _uiState.update {
                    it.copy(
                        currentTranscript = text,
                        recordingMillis = System.currentTimeMillis() - startTime
                    )
                }
            }
        }
    }

    fun onStopClicked() {
        if (!_uiState.value.isRecording) return
        transcriptJob?.cancel()
        viewModelScope.launch {
            val transcript = _uiState.value.currentTranscript
            stopRecordingUseCase(transcript)
            _uiState.update {
                it.copy(
                    isRecording = false,
                    currentTranscript = "",
                    recordingMillis = 0L
                )
            }
            _events.emit(
                if (transcript.isNotBlank())
                    VoiceNoteEvent.ShowSnackbar("Note saved")
                else
                    VoiceNoteEvent.ShowSnackbar("Empty note discarded")
            )
            loadAllNotes()
        }
    }

    fun editVoiceNote(noteId: Long, newText: String) {
        viewModelScope.launch {
            val note = _uiState.value.notes.find { it.id == noteId }
            if (note != null) {
                stopRecordingUseCase.repository.saveNote(
                    note.copy(text = newText, timestamp = System.currentTimeMillis())
                )
                _events.emit(VoiceNoteEvent.ShowSnackbar("Note updated"))
                loadAllNotes()
            }
        }
    }

    fun startEditTranscription(noteId: Long) {
        if (_uiState.value.isRecording || _uiState.value.isRecordingEditId != null) return
        transcriptJob?.cancel()
        val note = _uiState.value.notes.find { it.id == noteId }
        val oldText = note?.text ?: ""
        _uiState.update {
            it.copy(
                isRecordingEditId = noteId,
                currentTranscript = oldText,
                recordingMillis = 0L
            )
        }
        transcriptJob = viewModelScope.launch {
            _events.emit(VoiceNoteEvent.ShowSnackbar("Recording for edit started"))
            val startTime = System.currentTimeMillis()
            startRecordingUseCase().collect { newText ->
                // Append new text to old text
                _uiState.update {
                    it.copy(
                        currentTranscript = oldText + newText,
                        recordingMillis = System.currentTimeMillis() - startTime
                    )
                }
            }
        }
    }

    fun stopEditTranscription(noteId: Long) {
        if (_uiState.value.isRecordingEditId != noteId) return
        transcriptJob?.cancel()
        viewModelScope.launch {
            val transcript = _uiState.value.currentTranscript
            val note = _uiState.value.notes.find { it.id == noteId }
            if (note != null && transcript.isNotBlank()) {
                stopRecordingUseCase.repository.saveNote(
                    note.copy(text = transcript, timestamp = System.currentTimeMillis())
                )
                _events.emit(VoiceNoteEvent.ShowSnackbar("Note updated with audio"))
            } else if (note != null && transcript.isBlank()) {
                _events.emit(VoiceNoteEvent.ShowSnackbar("No new audio, note unchanged"))
            }
            loadAllNotes()
            _uiState.update {
                it.copy(
                    isRecordingEditId = null,
                    currentTranscript = "",
                    recordingMillis = 0L
                )
            }
        }
    }
}