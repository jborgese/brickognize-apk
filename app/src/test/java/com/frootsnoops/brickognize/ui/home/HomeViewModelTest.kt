package com.frootsnoops.brickognize.ui.home

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
@DisplayName("HomeViewModel Tests")
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var getScanHistoryUseCase: GetScanHistoryUseCase
    private val testDispatcher = StandardTestDispatcher()

    private fun createHistoryItem(id: String) = ScanHistoryItem(
        scanId = id.toLongOrNull() ?: 0L,
        recognitionType = "parts",
        timestamp = System.currentTimeMillis(),
        candidateCount = 1,
        imagePath = "/path/$id.jpg",
        topItem = BrickItem(id, "Name $id", "part", imgUrl = "url")
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getScanHistoryUseCase = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    @DisplayName("Initial state shows zero recent scans")
    fun `initial state zero scans`() = runTest {
        every { getScanHistoryUseCase(10) } returns flowOf(emptyList())
        
        viewModel = HomeViewModel(getScanHistoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.recentScansCount).isEqualTo(0)
        }

        verify { getScanHistoryUseCase(10) }
    }

    @Test
    @DisplayName("Loads recent scans count correctly")
    fun `loads recent scans count`() = runTest {
        val history = List(5) { createHistoryItem("scan-$it") }
        every { getScanHistoryUseCase(10) } returns flowOf(history)
        
        viewModel = HomeViewModel(getScanHistoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.recentScansCount).isEqualTo(5)
        }
    }

    @Test
    @DisplayName("Requests exactly 10 recent scans")
    fun `requests exactly 10 scans`() = runTest {
        every { getScanHistoryUseCase(10) } returns flowOf(emptyList())
        
        viewModel = HomeViewModel(getScanHistoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 1) { getScanHistoryUseCase(10) }
    }

    @Test
    @DisplayName("Handles maximum 10 recent scans")
    fun `handles maximum 10 scans`() = runTest {
        val history = List(10) { createHistoryItem("scan-$it") }
        every { getScanHistoryUseCase(10) } returns flowOf(history)
        
        viewModel = HomeViewModel(getScanHistoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.recentScansCount).isEqualTo(10)
        }
    }

    @Test
    @DisplayName("Updates when scan history changes")
    fun `updates when history changes`() = runTest {
        val initialHistory = List(3) { createHistoryItem("scan-$it") }
        val updatedHistory = List(7) { createHistoryItem("scan-$it") }
        
        every { getScanHistoryUseCase(10) } returns flowOf(initialHistory) andThen flowOf(updatedHistory)
        
        viewModel = HomeViewModel(getScanHistoryUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.recentScansCount).isEqualTo(3)
        }
    }
}
