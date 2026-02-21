package com.frootsnoops.brickognize.domain.usecase

import app.cash.turbine.test
import com.frootsnoops.brickognize.data.repository.ScanRepository
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.ScanHistoryItem
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("GetScanHistoryUseCase Tests")
class GetScanHistoryUseCaseTest {

    private lateinit var useCase: GetScanHistoryUseCase
    private lateinit var scanRepository: ScanRepository

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
        scanRepository = mockk()
        useCase = GetScanHistoryUseCase(scanRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("Returns flow of history items with specified limit")
    fun `returns history items with limit`() = runTest {
        every { scanRepository.getScanHistoryFlow(10) } returns flowOf(
            listOf(historyItem1, historyItem2)
        )

        useCase(10).test {
            val items = awaitItem()
            assertThat(items).hasSize(2)
            assertThat(items[0]).isEqualTo(historyItem1)
            assertThat(items[1]).isEqualTo(historyItem2)
            awaitComplete()
        }

        verify { scanRepository.getScanHistoryFlow(10) }
    }

    @Test
    @DisplayName("Returns empty list when no history exists")
    fun `returns empty list when no history`() = runTest {
        every { scanRepository.getScanHistoryFlow(50) } returns flowOf(emptyList())

        useCase(50).test {
            val items = awaitItem()
            assertThat(items).isEmpty()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("Respects limit parameter")
    fun `respects limit parameter`() = runTest {
        every { scanRepository.getScanHistoryFlow(5) } returns flowOf(
            List(5) { historyItem1 }
        )

        useCase(5).test {
            val items = awaitItem()
            assertThat(items).hasSize(5)
            awaitComplete()
        }

        verify { scanRepository.getScanHistoryFlow(5) }
    }

    @Test
    @DisplayName("Uses default limit of 50")
    fun `uses default limit of 50`() = runTest {
        every { scanRepository.getScanHistoryFlow(50) } returns flowOf(emptyList())

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        verify { scanRepository.getScanHistoryFlow(50) }
    }

    @Test
    @DisplayName("Flow updates when new history items added")
    fun `flow updates when history changes`() = runTest {
        val historyItem3 = historyItem1.copy(scanId = 3L)
        every { scanRepository.getScanHistoryFlow(10) } returns flowOf(
            listOf(historyItem1, historyItem2),
            listOf(historyItem3, historyItem1, historyItem2)
        )

        useCase(10).test {
            val first = awaitItem()
            assertThat(first).hasSize(2)

            val second = awaitItem()
            assertThat(second).hasSize(3)
            assertThat(second[0].scanId).isEqualTo(3L)

            awaitComplete()
        }
    }

    @Test
    @DisplayName("History items contain correct data")
    fun `history items contain correct data`() = runTest {
        every { scanRepository.getScanHistoryFlow(10) } returns flowOf(listOf(historyItem1))

        useCase(10).test {
            val items = awaitItem()
            val item = items[0]

            assertThat(item.scanId).isEqualTo(1L)
            assertThat(item.recognitionType).isEqualTo("parts")
            assertThat(item.candidateCount).isEqualTo(5)
            assertThat(item.topItem?.name).isEqualTo("Part 1")
            assertThat(item.topItem?.score).isEqualTo(0.95)

            awaitComplete()
        }
    }
}
