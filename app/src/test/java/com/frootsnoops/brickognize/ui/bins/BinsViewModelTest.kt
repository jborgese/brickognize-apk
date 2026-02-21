package com.frootsnoops.brickognize.ui.bins

import app.cash.turbine.test
import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.usecase.DeleteBinLocationUseCase
import com.frootsnoops.brickognize.domain.usecase.ExportBinLocationsUseCase
import com.frootsnoops.brickognize.domain.usecase.DeletePartUseCase
import com.frootsnoops.brickognize.domain.usecase.GetAllBinLocationsUseCase
import com.frootsnoops.brickognize.domain.usecase.GetPartsByBinUseCase
import com.frootsnoops.brickognize.domain.usecase.ImportBinLocationsUseCase
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
@DisplayName("BinsViewModel Tests")
class BinsViewModelTest {

    private lateinit var viewModel: BinsViewModel
    private lateinit var getAllBinLocationsUseCase: GetAllBinLocationsUseCase
    private lateinit var getPartsByBinUseCase: GetPartsByBinUseCase
    private lateinit var binLocationRepository: BinLocationRepository
    private lateinit var exportBinLocationsUseCase: ExportBinLocationsUseCase
    private lateinit var importBinLocationsUseCase: ImportBinLocationsUseCase
    private lateinit var deletePartUseCase: DeletePartUseCase
    private lateinit var deleteBinLocationUseCase: DeleteBinLocationUseCase
    private val testDispatcher = StandardTestDispatcher()

    private val bin1 = BinLocation(1L, "A1", "Top shelf", System.currentTimeMillis())
    private val bin2 = BinLocation(2L, "B2", "Bottom drawer", System.currentTimeMillis())
    private val part1 = BrickItem("p1", "Part 1", "part", imgUrl = "url1")
    private val part2 = BrickItem("p2", "Part 2", "part", imgUrl = "url2")

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllBinLocationsUseCase = mockk()
        getPartsByBinUseCase = mockk()
        binLocationRepository = mockk()
        exportBinLocationsUseCase = mockk()
        importBinLocationsUseCase = mockk()
        deletePartUseCase = mockk(relaxed = true)
        deleteBinLocationUseCase = mockk(relaxed = true)

        every { getAllBinLocationsUseCase() } returns flowOf(listOf(bin1, bin2))
        coEvery { binLocationRepository.getPartCountForBin(1L) } returns 5
        coEvery { binLocationRepository.getPartCountForBin(2L) } returns 3

        viewModel = BinsViewModel(
            getAllBinLocationsUseCase,
            getPartsByBinUseCase,
            binLocationRepository,
            exportBinLocationsUseCase,
            importBinLocationsUseCase,
            deletePartUseCase,
            deleteBinLocationUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    @DisplayName("Initial state loads bins with counts")
    fun `initial state loads bins with counts`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.bins).hasSize(2)
            assertThat(state.bins[0].binLocation).isEqualTo(bin1)
            assertThat(state.bins[0].partCount).isEqualTo(5)
            assertThat(state.bins[1].binLocation).isEqualTo(bin2)
            assertThat(state.bins[1].partCount).isEqualTo(3)
        }
    }

    @Test
    @DisplayName("Select bin loads parts for that bin")
    fun `selectBin loads parts for bin`() = runTest {
        every { getPartsByBinUseCase(1L) } returns flowOf(listOf(part1, part2))

        viewModel.selectBin(bin1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedBin).isEqualTo(bin1)
            assertThat(state.partsInSelectedBin).containsExactly(part1, part2)
        }

        verify { getPartsByBinUseCase(1L) }
    }

    @Test
    @DisplayName("Select bin with no parts returns empty list")
    fun `selectBin with no parts returns empty list`() = runTest {
        every { getPartsByBinUseCase(2L) } returns flowOf(emptyList())

        viewModel.selectBin(bin2)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedBin).isEqualTo(bin2)
            assertThat(state.partsInSelectedBin).isEmpty()
        }
    }

    @Test
    @DisplayName("Clear selection resets selected bin and parts")
    fun `clearSelection resets state`() = runTest {
        every { getPartsByBinUseCase(1L) } returns flowOf(listOf(part1))
        
        viewModel.selectBin(bin1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.clearSelection()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedBin).isNull()
            assertThat(state.partsInSelectedBin).isEmpty()
        }
    }

    @Test
    @DisplayName("Refresh reloads bins and counts")
    fun `refresh reloads bins`() = runTest {
        val bin3 = BinLocation(3L, "C3", "New bin", System.currentTimeMillis())
        
        // Change what getAllBinLocationsUseCase returns
        every { getAllBinLocationsUseCase() } returns flowOf(listOf(bin1, bin2, bin3))
        coEvery { binLocationRepository.getPartCountForBin(3L) } returns 7

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.bins).hasSize(3)
            assertThat(state.bins[2].binLocation).isEqualTo(bin3)
            assertThat(state.bins[2].partCount).isEqualTo(7)
        }
    }

    @Test
    @DisplayName("Loading state is true initially then false after load")
    fun `loading state transitions correctly`() = runTest {
        // Create new viewModel to catch initial loading state
        val newViewModel = BinsViewModel(
            getAllBinLocationsUseCase,
            getPartsByBinUseCase,
            binLocationRepository,
            exportBinLocationsUseCase,
            importBinLocationsUseCase,
            deletePartUseCase,
            deleteBinLocationUseCase
        )

        newViewModel.uiState.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isTrue()
            
            testDispatcher.scheduler.advanceUntilIdle()
            
            val loadedState = awaitItem()
            assertThat(loadedState.isLoading).isFalse()
        }
    }

    @Test
    @DisplayName("Selecting different bins updates parts list")
    fun `selecting different bins updates parts`() = runTest {
        val bin1Parts = listOf(part1)
        val bin2Parts = listOf(part2)
        
        every { getPartsByBinUseCase(1L) } returns flowOf(bin1Parts)
        every { getPartsByBinUseCase(2L) } returns flowOf(bin2Parts)

        viewModel.uiState.test {
            // Skip initial loading state
            skipItems(1)
            
            viewModel.selectBin(bin1)
            testDispatcher.scheduler.advanceUntilIdle()

            // First emission: selectedBin is set
            awaitItem()
            // Second emission: parts are loaded
            var state = awaitItem()
            assertThat(state.partsInSelectedBin).isEqualTo(bin1Parts)

            viewModel.selectBin(bin2)
            testDispatcher.scheduler.advanceUntilIdle()

            // First emission: selectedBin is updated
            awaitItem()
            // Second emission: new parts are loaded
            state = awaitItem()
            assertThat(state.partsInSelectedBin).isEqualTo(bin2Parts)
        }
    }

    @Test
    @DisplayName("Empty bins list shows no bins")
    fun `empty bins list shows no bins`() = runTest {
        every { getAllBinLocationsUseCase() } returns flowOf(emptyList())
        
        val emptyViewModel = BinsViewModel(
            getAllBinLocationsUseCase,
            getPartsByBinUseCase,
            binLocationRepository,
            exportBinLocationsUseCase,
            importBinLocationsUseCase,
            deletePartUseCase,
            deleteBinLocationUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        emptyViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.bins).isEmpty()
            assertThat(state.isLoading).isFalse()
        }
    }
}
