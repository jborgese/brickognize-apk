package com.frootsnoops.brickognize.ui.results

import app.cash.turbine.test
import android.content.Context
import coil.ImageLoader
import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.RecognitionResult
import com.frootsnoops.brickognize.domain.model.Result
import com.frootsnoops.brickognize.domain.usecase.AssignBinToPartUseCase
import com.frootsnoops.brickognize.domain.usecase.GetAllBinLocationsUseCase
import com.frootsnoops.brickognize.domain.usecase.GetPartByIdUseCase
import com.google.common.truth.Truth.assertThat
import com.frootsnoops.brickognize.domain.usecase.SubmitFeedbackUseCase
import androidx.lifecycle.SavedStateHandle
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
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("ResultsViewModel Tests")
class ResultsViewModelTest {

    private lateinit var viewModel: ResultsViewModel
    private lateinit var assignBinToPartUseCase: AssignBinToPartUseCase
    private lateinit var getAllBinLocationsUseCase: GetAllBinLocationsUseCase
    private lateinit var binLocationRepository: BinLocationRepository
    private lateinit var getPartByIdUseCase: GetPartByIdUseCase
    private lateinit var submitFeedbackUseCase: SubmitFeedbackUseCase
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var appContext: Context
    private lateinit var imageLoader: ImageLoader

    private val testBin1 = BinLocation(1L, "A1", "First bin", System.currentTimeMillis())
    private val testBin2 = BinLocation(2L, "B2", "Second bin", System.currentTimeMillis())
    private val testPart = BrickItem("part-1", "Test Part", "part", imgUrl = "test.jpg", score = 0.95)

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        appContext = mockk(relaxed = true)
        imageLoader = mockk(relaxed = true)
        assignBinToPartUseCase = mockk()
        getAllBinLocationsUseCase = mockk()
        binLocationRepository = mockk()
        getPartByIdUseCase = mockk()
        submitFeedbackUseCase = mockk(relaxed = true)

        every { getAllBinLocationsUseCase() } returns flowOf(listOf(testBin1, testBin2))
        every { binLocationRepository.getBinLatestPartUpdatesFlow() } returns flowOf(
            mapOf(
                testBin1.id to testBin1.createdAt,
                testBin2.id to testBin2.createdAt
            )
        )

        viewModel = ResultsViewModel(
            appContext = appContext,
            imageLoader = imageLoader,
            assignBinToPartUseCase = assignBinToPartUseCase,
            getAllBinLocationsUseCase = getAllBinLocationsUseCase,
            binLocationRepository = binLocationRepository,
            getPartByIdUseCase = getPartByIdUseCase,
            submitFeedbackUseCase = submitFeedbackUseCase,
            appPreferencesRepository = mockk(relaxed = true),
            savedStateHandle = SavedStateHandle()
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    @DisplayName("Initial state has empty recognition result and loads bins")
    fun `initial state loads bins`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.recognitionResult).isNull()
            assertThat(state.availableBins).containsExactly(testBin1, testBin2)
            assertThat(state.binLastModifiedAt.keys).containsExactly(testBin1.id, testBin2.id)
            assertThat(state.showBinPicker).isFalse()
            assertThat(state.isAssigningBin).isFalse()
        }
    }

    @Test
    @DisplayName("Setting recognition result updates state")
    fun `setRecognitionResult updates state`() = runTest {
        val result = RecognitionResult(
            "test-123",
            testPart,
            listOf(testPart)
        )

        viewModel.setRecognitionResult(result)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.recognitionResult).isEqualTo(result)
        }
    }

    @Test
    @DisplayName("Show bin picker sets selectedPartId and shows picker")
    fun `showBinPicker sets state correctly`() = runTest {
        viewModel.showBinPicker("part-123")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.showBinPicker).isTrue()
            assertThat(state.selectedPartId).isEqualTo("part-123")
        }
    }

    @Test
    @DisplayName("Hide bin picker clears selection")
    fun `hideBinPicker clears selection`() = runTest {
        viewModel.showBinPicker("part-123")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.hideBinPicker()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.showBinPicker).isFalse()
            assertThat(state.selectedPartId).isNull()
        }
    }

    @Test
    @DisplayName("Assign existing bin succeeds and refreshes part")
    fun `assignBinToPart with existing bin succeeds`() = runTest {
        val partId = "part-456"
        viewModel.showBinPicker(partId)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedPart = testPart.copy(id = partId, binLocation = testBin1)
        coEvery { assignBinToPartUseCase(partId, 1L, null, null) } returns Result.Success(Unit)
        coEvery { getPartByIdUseCase.getOnce(partId) } returns updatedPart

        val recognitionResult = RecognitionResult(
            "scan-1",
            testPart.copy(id = partId),
            listOf(testPart.copy(id = partId))
        )
        viewModel.setRecognitionResult(recognitionResult)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.assignBinToPart(binId = 1L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isAssigningBin).isFalse()
            assertThat(state.showBinPicker).isFalse()
            assertThat(state.selectedPartId).isNull()
            assertThat(state.recognitionResult?.topCandidate?.binLocation).isEqualTo(testBin1)
        }

        coVerify { assignBinToPartUseCase(partId, 1L, null, null) }
        coVerify { getPartByIdUseCase.getOnce(partId) }
    }

    @Test
    @DisplayName("Assign new bin succeeds")
    fun `assignBinToPart with new bin label succeeds`() = runTest {
        val partId = "part-789"
        viewModel.showBinPicker(partId)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { 
            assignBinToPartUseCase(partId, null, "C3", "New bin") 
        } returns Result.Success(Unit)
        coEvery { getPartByIdUseCase.getOnce(partId) } returns testPart.copy(id = partId)

        viewModel.assignBinToPart(binId = null, newBinLabel = "C3", newBinDescription = "New bin")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isAssigningBin).isFalse()
            assertThat(state.showBinPicker).isFalse()
        }

        coVerify { assignBinToPartUseCase(partId, null, "C3", "New bin") }
    }

    @Test
    @DisplayName("Assign bin error shows error message")
    fun `assignBinToPart error shows error message`() = runTest {
        val partId = "part-error"
        viewModel.showBinPicker(partId)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { 
            assignBinToPartUseCase(partId, 1L, null, null) 
        } returns Result.Error(Exception("DB error"), "Failed to assign bin")

        viewModel.assignBinToPart(binId = 1L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isAssigningBin).isFalse()
            assertThat(state.error).isNotNull()
            assertThat(state.error!!.message).contains("DB error")
        }
    }

    @Test
    @DisplayName("Assign bin without selected part does nothing")
    fun `assignBinToPart without selectedPartId does nothing`() = runTest {
        viewModel.assignBinToPart(binId = 1L)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { assignBinToPartUseCase(any(), any(), any(), any()) }
    }

    @Test
    @DisplayName("Clear error removes error message")
    fun `clearError removes error message`() = runTest {
        viewModel.showBinPicker("part-1")
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { 
            assignBinToPartUseCase(any(), any(), any(), any()) 
        } returns Result.Error(Exception(), "Test error")

        viewModel.assignBinToPart(binId = 1L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.error).isNull()
        }
    }

    @Test
    @DisplayName("Refresh part bin location updates multiple candidates")
    fun `refreshPartBinLocation updates all matching candidates`() = runTest {
        val partId = "shared-part"
        val part1 = testPart.copy(id = partId, name = "Part 1")
        val part2 = testPart.copy(id = "other-part", name = "Part 2")
        
        val recognitionResult = RecognitionResult(
            "scan-multi",
            part1,
            listOf(part1, part2, part1.copy(name = "Part 1 Variant"))
        )
        
        viewModel.setRecognitionResult(recognitionResult)
        viewModel.showBinPicker(partId)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedPart = part1.copy(binLocation = testBin2)
        coEvery { assignBinToPartUseCase(partId, 2L, null, null) } returns Result.Success(Unit)
        coEvery { getPartByIdUseCase.getOnce(partId) } returns updatedPart

        viewModel.assignBinToPart(binId = 2L)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val candidates = state.recognitionResult?.candidates
            assertThat(candidates?.get(0)?.binLocation).isEqualTo(testBin2)
            assertThat(candidates?.get(1)?.binLocation).isNull()
            assertThat(candidates?.get(2)?.binLocation).isEqualTo(testBin2)
        }
    }
}
