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
    data class ShowSnackBar(val message: String) : VoiceNoteEvent()
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
            runCatching { getAllNotesUseCase() }
                .onSuccess { notes ->
                    notes.collect { noteList ->
                        _uiState.update { it.copy(notes = noteList) }
                    }
                }.onFailure {
                    _events.emit(VoiceNoteEvent.ShowSnackBar("Error loading notes"))
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
            _events.emit(VoiceNoteEvent.ShowSnackBar("Recording started"))
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
                    VoiceNoteEvent.ShowSnackBar("Note saved")
                else
                    VoiceNoteEvent.ShowSnackBar("Empty note discarded")
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
                _events.emit(VoiceNoteEvent.ShowSnackBar("Note updated"))
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
            _events.emit(VoiceNoteEvent.ShowSnackBar("Recording for edit started"))
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
            val originalText = note?.text ?: ""

            // Check if transcript is different from original text
            if (note != null && transcript != originalText) {
                stopRecordingUseCase.repository.saveNote(
                    note.copy(text = transcript, timestamp = System.currentTimeMillis())
                )
                _events.emit(VoiceNoteEvent.ShowSnackBar("Note updated with audio"))
            } else if (note != null) {
                _events.emit(VoiceNoteEvent.ShowSnackBar("No new audio, note unchanged"))
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