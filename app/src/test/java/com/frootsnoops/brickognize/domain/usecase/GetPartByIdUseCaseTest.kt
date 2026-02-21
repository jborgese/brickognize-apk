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
@DisplayName("GetPartByIdUseCase Tests")
class GetPartByIdUseCaseTest {

    private lateinit var useCase: GetPartByIdUseCase
    private lateinit var partRepository: PartRepository

    private val binLocation = BinLocation(1L, "A1", "Top shelf", System.currentTimeMillis())
    private val partWithBin = BrickItem(
        id = "p123",
        name = "Brick 2x4",
        type = "part",
        imgUrl = "url",
        binLocation = binLocation
    )
    private val partWithoutBin = BrickItem(
        id = "p456",
        name = "Plate 1x2",
        type = "part",
        imgUrl = "url2",
        binLocation = null
    )

    @BeforeEach
    fun setup() {
        partRepository = mockk()
        useCase = GetPartByIdUseCase(partRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("Returns flow of part with bin location")
    fun `returns flow of part with bin`() = runTest {
        every { partRepository.getPartByIdFlow("p123") } returns flowOf(partWithBin)

        useCase("p123").test {
            val part = awaitItem()
            assertThat(part).isEqualTo(partWithBin)
            assertThat(part?.binLocation).isEqualTo(binLocation)
            awaitComplete()
        }

        verify { partRepository.getPartByIdFlow("p123") }
    }

    @Test
    @DisplayName("Returns flow of part without bin location")
    fun `returns flow of part without bin`() = runTest {
        every { partRepository.getPartByIdFlow("p456") } returns flowOf(partWithoutBin)

        useCase("p456").test {
            val part = awaitItem()
            assertThat(part).isEqualTo(partWithoutBin)
            assertThat(part?.binLocation).isNull()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("Returns flow of null when part not found")
    fun `returns null when part not found`() = runTest {
        every { partRepository.getPartByIdFlow("unknown") } returns flowOf(null)

        useCase("unknown").test {
            val part = awaitItem()
            assertThat(part).isNull()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getOnce returns single part with bin")
    fun `getOnce returns single part with bin`() = runTest {
        coEvery { partRepository.getPartById("p123") } returns partWithBin

        val result = useCase.getOnce("p123")

        assertThat(result).isEqualTo(partWithBin)
        assertThat(result?.binLocation).isEqualTo(binLocation)

        coVerify { partRepository.getPartById("p123") }
    }

    @Test
    @DisplayName("getOnce returns null when part not found")
    fun `getOnce returns null when not found`() = runTest {
        coEvery { partRepository.getPartById("unknown") } returns null

        val result = useCase.getOnce("unknown")

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("Flow updates when part bin location changes")
    fun `flow updates when bin location changes`() = runTest {
        val partUpdated = partWithoutBin.copy(binLocation = binLocation)
        every { partRepository.getPartByIdFlow("p456") } returns flowOf(partWithoutBin, partUpdated)

        useCase("p456").test {
            val first = awaitItem()
            assertThat(first?.binLocation).isNull()

            val second = awaitItem()
            assertThat(second?.binLocation).isEqualTo(binLocation)

            awaitComplete()
        }
    }
}
