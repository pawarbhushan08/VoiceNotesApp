package com.bhushan.android.presentation

import com.bhushan.android.domain.model.VoiceNote
import com.bhushan.android.domain.usecase.GetAllNotesUseCase
import com.bhushan.android.domain.usecase.StartRecordingUseCase
import com.bhushan.android.domain.usecase.StopRecordingUseCase
import com.bhushan.android.presentation.rule.MainDispatcherRule
import com.bhushan.android.presentation.vm.VoiceNoteViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class VoiceNoteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeVoiceNoteRepository
    private lateinit var fakeTranscriptionService: FakeTranscriptionService
    private lateinit var startRecordingUseCase: StartRecordingUseCase
    private lateinit var stopRecordingUseCase: StopRecordingUseCase
    private lateinit var getAllNotesUseCase: GetAllNotesUseCase
    private lateinit var viewModel: VoiceNoteViewModel

    private val sampleNotes = listOf(
        VoiceNote(id = 1L, text = "First note", timestamp = 1000L),
        VoiceNote(id = 2L, text = "Second note", timestamp = 2000L)
    )

    @Before
    fun setup() {
        fakeRepository = FakeVoiceNoteRepository()
        fakeTranscriptionService = FakeTranscriptionService()

        // Add sample notes
        sampleNotes.forEach { fakeRepository.addNote(it) }

        startRecordingUseCase = StartRecordingUseCase(fakeTranscriptionService)
        stopRecordingUseCase = StopRecordingUseCase(fakeRepository)
        getAllNotesUseCase = GetAllNotesUseCase(fakeRepository)

        viewModel = VoiceNoteViewModel(
            startRecordingUseCase,
            stopRecordingUseCase,
            getAllNotesUseCase
        )
    }

    @Test
    fun `initial state should load notes successfully`() = runTest {
        // When - ViewModel is initialized
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.notes.size)
        assertEquals("First note", state.notes[0].text)
        assertEquals("Second note", state.notes[1].text)
        assertFalse(state.isRecording)
        assertEquals("", state.currentTranscript)
        assertEquals(0L, state.recordingMillis)
        assertNull(state.isRecordingEditId)
    }

    @Test
    fun `initial state should handle error when loading notes fails`() = runTest {
        // Given
        fakeRepository.setShouldThrowError(true)

        // When
        val newViewModel = VoiceNoteViewModel(
            startRecordingUseCase,
            stopRecordingUseCase,
            getAllNotesUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = newViewModel.uiState.value
        assertTrue(state.notes.isEmpty())
    }

    @Test
    fun `onRecordClicked should start recording when not already recording`() = runTest {
        // Given
        fakeTranscriptionService.setTranscriptionText("Hello world")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onRecordClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isRecording)
        assertEquals("Hello world", state.currentTranscript)
        assertTrue(state.recordingMillis >= 0L)
    }

    @Test
    fun `onRecordClicked should not start recording when already recording`() = runTest {
        // Given - start recording first
        fakeTranscriptionService.setTranscriptionText("Test")
        viewModel.onRecordClicked()
        testDispatcher.scheduler.advanceUntilIdle()
        val initialState = viewModel.uiState.value

        // When - try to start recording again
        viewModel.onRecordClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - state should remain unchanged
        val finalState = viewModel.uiState.value
        assertEquals(initialState.isRecording, finalState.isRecording)
        assertEquals(initialState.currentTranscript, finalState.currentTranscript)
    }

    @Test
    fun `onRecordClicked should not start when editing another note`() = runTest {
        // Given - start editing a note
        fakeTranscriptionService.setTranscriptionText("Edit text")
        viewModel.startEditTranscription(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - try to start new recording
        viewModel.onRecordClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should still be in edit mode, not normal recording
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertEquals(1L, state.isRecordingEditId)
    }

    @Test
    fun `onStopClicked should stop recording and save non-empty transcript`() = runTest {
        // Given - start recording with transcript
        fakeTranscriptionService.setTranscriptionText("Important note")
        viewModel.onRecordClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onStopClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(fakeRepository.saveNoteCalled)
        assertEquals("Important note", fakeRepository.lastSavedNote?.text)
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertEquals("", state.currentTranscript)
        assertEquals(0L, state.recordingMillis)
    }

    @Test
    fun `onStopClicked should stop recording and not save empty transcript`() = runTest {
        // Given - start recording with empty transcript
        fakeTranscriptionService.setTranscriptionText("")
        viewModel.onRecordClicked()
        testDispatcher.scheduler.advanceUntilIdle()
        fakeRepository.clear()

        // When
        viewModel.onStopClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should not save empty note
        assertFalse(fakeRepository.saveNoteCalled)
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
    }

    @Test
    fun `onStopClicked should do nothing when not recording`() = runTest {
        // Given - not recording
        testDispatcher.scheduler.advanceUntilIdle()
        fakeRepository.clear()

        // When
        viewModel.onStopClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - state should remain unchanged and no save should occur
        val state = viewModel.uiState.value
        assertFalse(state.isRecording)
        assertEquals("", state.currentTranscript)
        assertFalse(fakeRepository.saveNoteCalled)
    }

    @Test
    fun `editVoiceNote should update existing note`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle()
        val newText = "Updated note text"
        fakeRepository.clear()

        // When
        viewModel.editVoiceNote(1L, newText)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(fakeRepository.saveNoteCalled)
        assertEquals(newText, fakeRepository.lastSavedNote?.text)
        assertEquals(1L, fakeRepository.lastSavedNote?.id)
    }

    @Test
    fun `editVoiceNote should do nothing for non-existent note`() = runTest {
        // Given
        testDispatcher.scheduler.advanceUntilIdle()
        fakeRepository.clear()

        // When
        viewModel.editVoiceNote(999L, "New text")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - saveNote should not be called
        assertFalse(fakeRepository.saveNoteCalled)
    }

    @Test
    fun `startEditTranscription should start edit mode for existing note`() = runTest {
        // Given
        fakeTranscriptionService.setTranscriptionText(" additional text")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.startEditTranscription(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1L, state.isRecordingEditId)
        assertTrue(state.currentTranscript.contains("First note"))
        assertTrue(state.currentTranscript.contains("additional text"))
    }

    @Test
    fun `startEditTranscription should not start when already recording`() = runTest {
        // Given - start normal recording
        fakeTranscriptionService.setTranscriptionText("Recording")
        viewModel.onRecordClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.startEditTranscription(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isRecording)
        assertNull(state.isRecordingEditId)
    }

    @Test
    fun `stopEditTranscription should update note with new content`() = runTest {
        // Given - start edit transcription
        fakeTranscriptionService.setTranscriptionText(" updated content")
        viewModel.startEditTranscription(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeRepository.clear()

        // When
        viewModel.stopEditTranscription(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(fakeRepository.saveNoteCalled)
        assertTrue(fakeRepository.lastSavedNote?.text?.contains("First note") == true)
        assertTrue(fakeRepository.lastSavedNote?.text?.contains("updated content") == true)
        val state = viewModel.uiState.value
        assertNull(state.isRecordingEditId)
        assertEquals("", state.currentTranscript)
        assertEquals(0L, state.recordingMillis)
    }

    @Test
    fun `stopEditTranscription should not update note when transcript is blank`() = runTest {
        // Given - start edit with empty transcript
        fakeTranscriptionService.setTranscriptionText("")
        viewModel.startEditTranscription(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        fakeRepository.clear()

        // When
        viewModel.stopEditTranscription(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - note should not be saved since transcript is effectively empty
        assertFalse(fakeRepository.saveNoteCalled)
        val state = viewModel.uiState.value
        assertNull(state.isRecordingEditId)
    }

    @Test
    fun `stopEditTranscription should do nothing for wrong note ID`() = runTest {
        // Given - start edit for note 1
        fakeTranscriptionService.setTranscriptionText("test")
        viewModel.startEditTranscription(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - try to stop edit for note 2
        viewModel.stopEditTranscription(2L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should still be in edit mode for note 1
        val state = viewModel.uiState.value
        assertEquals(1L, state.isRecordingEditId)
    }
}