package com.frootsnoops.brickognize.ui.history

import app.cash.turbine.test
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.ScanHistoryItem
import com.frootsnoops.brickognize.domain.usecase.GetScanHistoryUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("HistoryViewModel Tests")
class HistoryViewModelTest {

    private lateinit var viewModel: HistoryViewModel
    private lateinit var getScanHistoryUseCase: GetScanHistoryUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val historyItem1 = ScanHistoryItem(
        scanId = 1L,
        recognitionType = "parts",
        timestamp = System.currentTimeMillis(),
        candidateCount = 5,
        imagePath = "/path/image1.jpg",
        topItem = BrickItem("p1", "Part 1", "part", imgUrl = "url1", score = 0.95)
    )
    
    private val historyItem2 = ScanHistoryItem(
        scanId = 2L,
        recognitionType = "sets",
        timestamp = System.currentTimeMillis() - 1000,
        candidateCount = 3,
        imagePath = "/path/image2.jpg",
        topItem = BrickItem("s1", "Set 1", "set", imgUrl = "url2", score = 0.88)
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getScanHistoryUseCase = mockk()
        
        every { getScanHistoryUseCase(50) } returns flowOf(listOf(historyItem1, historyItem2))

        viewModel = HistoryViewModel(getScanHistoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    @DisplayName("Initial state loads history items")
    fun `initial state loads history`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.historyItems).hasSize(2)
            assertThat(state.historyItems[0]).isEqualTo(historyItem1)
            assertThat(state.historyItems[1]).isEqualTo(historyItem2)
        }

        verify { getScanHistoryUseCase(50) }
    }

    @Test
    @DisplayName("Loading state transitions correctly")
    fun `loading state transitions`() = runTest {
        val newViewModel = HistoryViewModel(getScanHistoryUseCase)

        newViewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isTrue()
            assertThat(initialState.historyItems).isEmpty()

            testDispatcher.scheduler.advanceUntilIdle()

            val loadedState = awaitItem()
            assertThat(loadedState.isLoading).isFalse()
            assertThat(loadedState.historyItems).isNotEmpty()
        }
    }

    @Test
    @DisplayName("Empty history shows no items")
    fun `empty history shows no items`() = runTest {
        every { getScanHistoryUseCase(50) } returns flowOf(emptyList())
        
        val emptyViewModel = HistoryViewModel(getScanHistoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        emptyViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.historyItems).isEmpty()
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    @DisplayName("Refresh reloads history")
    fun `refresh reloads history`() = runTest {
        val historyItem3 = ScanHistoryItem(
            scanId = 3L,
            recognitionType = "figs",
            timestamp = System.currentTimeMillis(),
            candidateCount = 1,
            imagePath = "/path/image3.jpg",
            topItem = BrickItem("f1", "Fig 1", "fig", imgUrl = "url3", score = 0.99)
        )
        
        every { getScanHistoryUseCase(50) } returns flowOf(
            listOf(historyItem3, historyItem1, historyItem2)
        )

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.historyItems).hasSize(3)
            assertThat(state.historyItems[0]).isEqualTo(historyItem3)
        }

        verify(atLeast = 2) { getScanHistoryUseCase(50) }
    }

    @Test
    @DisplayName("History items maintain correct order")
    fun `history items maintain order`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.historyItems[0].scanId).isEqualTo(1L)
            assertThat(state.historyItems[1].scanId).isEqualTo(2L)
        }
    }

    @Test
    @DisplayName("History items contain correct data")
    fun `history items contain correct data`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            val first = state.historyItems[0]
            
            assertThat(first.scanId).isEqualTo(1L)
            assertThat(first.recognitionType).isEqualTo("parts")
            assertThat(first.candidateCount).isEqualTo(5)
            assertThat(first.topItem?.name).isEqualTo("Part 1")
            assertThat(first.topItem?.score).isEqualTo(0.95)
        }
    }
}
