package com.bhushan.android.presentation.usecase

import com.bhushan.android.domain.model.VoiceNote
import com.bhushan.android.domain.usecase.GetAllNotesUseCase
import com.bhushan.android.presentation.FakeVoiceNoteRepository
import junit.framework.TestCase.assertEquals
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
class GetAllNotesUseCaseTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeVoiceNoteRepository
    private lateinit var useCase: GetAllNotesUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeVoiceNoteRepository()
        useCase = GetAllNotesUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke should return flow with notes from repository`() = runTest {
        // Given
        val expectedNotes = listOf(
            VoiceNote(id = 1L, text = "Note 1", timestamp = 1000L),
            VoiceNote(id = 2L, text = "Note 2", timestamp = 2000L)
        )
        expectedNotes.forEach { fakeRepository.addNote(it) }

        // When
        val resultFlow = useCase()

        // Then
        resultFlow.collect { notes ->
            assertEquals(2, notes.size)
            assertEquals("Note 1", notes[0].text)
            assertEquals("Note 2", notes[1].text)
        }
    }

    @Test
    fun `invoke should return empty flow when no notes exist`() = runTest {
        // Given - empty repository

        // When
        val resultFlow = useCase()

        // Then
        resultFlow.collect { notes ->
            assertTrue(notes.isEmpty())
        }
    }

    @Test
    fun `invoke should propagate repository exceptions`() = runTest {
        // Given
        fakeRepository.setShouldThrowError(true)

        // When & Then
        try {
            useCase().collect { }
            // Should not reach here
            assertTrue(false)
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }
    }
}
