package com.frootsnoops.brickognize.domain.usecase

import app.cash.turbine.test
import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
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
@DisplayName("GetAllBinLocationsUseCase Tests")
class GetAllBinLocationsUseCaseTest {

    private lateinit var useCase: GetAllBinLocationsUseCase
    private lateinit var binLocationRepository: BinLocationRepository

    private val bin1 = BinLocation(1L, "A1", "Top shelf", System.currentTimeMillis())
    private val bin2 = BinLocation(2L, "B2", "Bottom drawer", System.currentTimeMillis())
    private val bin3 = BinLocation(3L, "C3", "Middle rack", System.currentTimeMillis())

    @BeforeEach
    fun setup() {
        binLocationRepository = mockk()
        useCase = GetAllBinLocationsUseCase(binLocationRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("Returns flow of all bin locations")
    fun `returns all bin locations`() = runTest {
        every { binLocationRepository.getAllBinLocationsFlow() } returns flowOf(
            listOf(bin1, bin2, bin3)
        )

        useCase().test {
            val bins = awaitItem()
            assertThat(bins).hasSize(3)
            assertThat(bins).containsExactly(bin1, bin2, bin3).inOrder()
            awaitComplete()
        }

        verify { binLocationRepository.getAllBinLocationsFlow() }
    }

    @Test
    @DisplayName("Returns empty list when no bins exist")
    fun `returns empty list when no bins`() = runTest {
        every { binLocationRepository.getAllBinLocationsFlow() } returns flowOf(emptyList())

        useCase().test {
            val bins = awaitItem()
            assertThat(bins).isEmpty()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("Flow updates when new bins added")
    fun `flow updates when bins added`() = runTest {
        every { binLocationRepository.getAllBinLocationsFlow() } returns flowOf(
            listOf(bin1, bin2),
            listOf(bin1, bin2, bin3)
        )

        useCase().test {
            val first = awaitItem()
            assertThat(first).hasSize(2)

            val second = awaitItem()
            assertThat(second).hasSize(3)
            assertThat(second.last()).isEqualTo(bin3)

            awaitComplete()
        }
    }

    @Test
    @DisplayName("Flow updates when bins deleted")
    fun `flow updates when bins deleted`() = runTest {
        every { binLocationRepository.getAllBinLocationsFlow() } returns flowOf(
            listOf(bin1, bin2, bin3),
            listOf(bin1, bin3)
        )

        useCase().test {
            val first = awaitItem()
            assertThat(first).hasSize(3)

            val second = awaitItem()
            assertThat(second).hasSize(2)
            assertThat(second).doesNotContain(bin2)

            awaitComplete()
        }
    }

    @Test
    @DisplayName("Bin locations contain correct data")
    fun `bin locations contain correct data`() = runTest {
        every { binLocationRepository.getAllBinLocationsFlow() } returns flowOf(listOf(bin1))

        useCase().test {
            val bins = awaitItem()
            val bin = bins[0]

            assertThat(bin.id).isEqualTo(1L)
            assertThat(bin.label).isEqualTo("A1")
            assertThat(bin.description).isEqualTo("Top shelf")

            awaitComplete()
        }
    }

    @Test
    @DisplayName("Returns single bin")
    fun `returns single bin`() = runTest {
        every { binLocationRepository.getAllBinLocationsFlow() } returns flowOf(listOf(bin1))

        useCase().test {
            val bins = awaitItem()
            assertThat(bins).hasSize(1)
            assertThat(bins[0]).isEqualTo(bin1)
            awaitComplete()
        }
    }
}
