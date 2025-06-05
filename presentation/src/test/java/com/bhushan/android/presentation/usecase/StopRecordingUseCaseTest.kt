package com.bhushan.android.presentation.usecase

import com.bhushan.android.domain.usecase.StopRecordingUseCase
import com.bhushan.android.presentation.FakeVoiceNoteRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class StopRecordingUseCaseTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeVoiceNoteRepository
    private lateinit var useCase: StopRecordingUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeVoiceNoteRepository()
        useCase = StopRecordingUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke should save note when transcript is not blank`() = runTest {
        // Given
        val transcript = "This is a voice note"

        // When
        useCase(transcript)

        // Then
        assertTrue(fakeRepository.saveNoteCalled)
        assertEquals(transcript, fakeRepository.lastSavedNote?.text)
        assertTrue((fakeRepository.lastSavedNote?.timestamp ?: 0L) > 0L)
    }

    @Test
    fun `invoke should not save note when transcript is blank`() = runTest {
        // Given
        val transcript = ""

        // When
        useCase(transcript)

        // Then
        assertFalse(fakeRepository.saveNoteCalled)
        assertNull(fakeRepository.lastSavedNote)
    }

    @Test
    fun `invoke should not save note when transcript is only whitespace`() = runTest {
        // Given
        val transcript = "   \n\t  "

        // When
        useCase(transcript)

        // Then
        assertFalse(fakeRepository.saveNoteCalled)
        assertNull(fakeRepository.lastSavedNote)
    }

    @Test
    fun `invoke should create note with current timestamp`() = runTest {
        // Given
        val transcript = "Test note"
        val beforeTime = System.currentTimeMillis()

        // When
        useCase(transcript)

        // Then
        val afterTime = System.currentTimeMillis()
        assertTrue(fakeRepository.saveNoteCalled)
        val savedNote = fakeRepository.lastSavedNote
        assertTrue((savedNote?.timestamp ?: 0L) >= beforeTime)
        assertTrue((savedNote?.timestamp ?: Long.MAX_VALUE) <= afterTime)
    }
}