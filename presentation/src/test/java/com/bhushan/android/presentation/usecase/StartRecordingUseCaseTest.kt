package com.bhushan.android.presentation.usecase

import com.bhushan.android.domain.usecase.StartRecordingUseCase
import com.bhushan.android.presentation.FakeTranscriptionService
import junit.framework.TestCase.assertEquals
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
class StartRecordingUseCaseTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeTranscriptionService: FakeTranscriptionService
    private lateinit var useCase: StartRecordingUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeTranscriptionService = FakeTranscriptionService()
        useCase = StartRecordingUseCase(fakeTranscriptionService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke should return flow with transcribed text`() = runTest {
        // Given
        val expectedText = "transcribed text"
        fakeTranscriptionService.setTranscriptionText(expectedText)

        // When
        val resultFlow = useCase()

        // Then
        resultFlow.collect { text ->
            assertEquals(expectedText, text)
        }
    }

    @Test
    fun `invoke should return empty flow when no transcription available`() = runTest {
        // Given
        fakeTranscriptionService.setShouldEmitText(false)

        // When
        val resultFlow = useCase()

        // Then
        resultFlow.collect { text ->
            assertEquals("", text)
        }
    }
}
