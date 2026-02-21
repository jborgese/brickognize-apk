package com.frootsnoops.brickognize.domain.usecase

import app.cash.turbine.test
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.BrickItem
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
@DisplayName("GetPartsByBinUseCase Tests")
class GetPartsByBinUseCaseTest {

    private lateinit var useCase: GetPartsByBinUseCase
    private lateinit var partRepository: PartRepository

    private val binLocation = BinLocation(1L, "A1", "Top shelf", System.currentTimeMillis())
    
    private val part1 = BrickItem(
        id = "p1",
        name = "Brick 2x4",
        type = "part",
        imgUrl = "url1",
        binLocation = binLocation
    )
    
    private val part2 = BrickItem(
        id = "p2",
        name = "Plate 1x2",
        type = "part",
        imgUrl = "url2",
        binLocation = binLocation
    )

    private val part3 = BrickItem(
        id = "p3",
        name = "Tile 2x2",
        type = "part",
        imgUrl = "url3",
        binLocation = binLocation
    )

    @BeforeEach
    fun setup() {
        partRepository = mockk()
        useCase = GetPartsByBinUseCase(partRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("Returns flow of parts for specific bin")
    fun `returns parts for bin`() = runTest {
        every { partRepository.getPartsByBinLocationFlow(1L) } returns flowOf(listOf(part1, part2, part3))

        useCase(1L).test {
            val parts = awaitItem()
            assertThat(parts).hasSize(3)
            assertThat(parts).containsExactly(part1, part2, part3).inOrder()
            awaitComplete()
        }

        verify { partRepository.getPartsByBinLocationFlow(1L) }
    }

    @Test
    @DisplayName("Returns empty list when bin has no parts")
    fun `returns empty list when bin has no parts`() = runTest {
        every { partRepository.getPartsByBinLocationFlow(1L) } returns flowOf(emptyList())

        useCase(1L).test {
            val parts = awaitItem()
            assertThat(parts).isEmpty()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("Flow updates when parts added to bin")
    fun `flow updates when parts added to bin`() = runTest {
        every { partRepository.getPartsByBinLocationFlow(1L) } returns flowOf(
            listOf(part1),
            listOf(part1, part2)
        )

        useCase(1L).test {
            val first = awaitItem()
            assertThat(first).hasSize(1)

            val second = awaitItem()
            assertThat(second).hasSize(2)
            assertThat(second.last()).isEqualTo(part2)

            awaitComplete()
        }
    }

    @Test
    @DisplayName("Flow updates when parts removed from bin")
    fun `flow updates when parts removed from bin`() = runTest {
        every { partRepository.getPartsByBinLocationFlow(1L) } returns flowOf(
            listOf(part1, part2, part3),
            listOf(part1, part3)
        )

        useCase(1L).test {
            val first = awaitItem()
            assertThat(first).hasSize(3)

            val second = awaitItem()
            assertThat(second).hasSize(2)
            assertThat(second).doesNotContain(part2)

            awaitComplete()
        }
    }

    @Test
    @DisplayName("Parts contain correct bin location")
    fun `parts contain correct bin location`() = runTest {
        every { partRepository.getPartsByBinLocationFlow(1L) } returns flowOf(listOf(part1))

        useCase(1L).test {
            val parts = awaitItem()
            val part = parts[0]

            assertThat(part.binLocation).isEqualTo(binLocation)
            assertThat(part.binLocation?.label).isEqualTo("A1")

            awaitComplete()
        }
    }

    @Test
    @DisplayName("Returns single part in bin")
    fun `returns single part in bin`() = runTest {
        every { partRepository.getPartsByBinLocationFlow(1L) } returns flowOf(listOf(part1))

        useCase(1L).test {
            val parts = awaitItem()
            assertThat(parts).hasSize(1)
            assertThat(parts[0].id).isEqualTo("p1")
            awaitComplete()
        }
    }

    @Test
    @DisplayName("Handles different bin IDs")
    fun `handles different bin IDs`() = runTest {
        val bin2 = BinLocation(2L, "B2", "Bottom drawer", System.currentTimeMillis())
        val part4 = part1.copy(id = "p4", binLocation = bin2)
        
        every { partRepository.getPartsByBinLocationFlow(2L) } returns flowOf(listOf(part4))

        useCase(2L).test {
            val parts = awaitItem()
            assertThat(parts[0].binLocation?.id).isEqualTo(2L)
            assertThat(parts[0].binLocation?.label).isEqualTo("B2")
            awaitComplete()
        }

        verify { partRepository.getPartsByBinLocationFlow(2L) }
    }
}
