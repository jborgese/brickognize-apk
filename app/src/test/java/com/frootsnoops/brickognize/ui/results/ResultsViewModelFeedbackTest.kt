package com.frootsnoops.brickognize.ui.results

import android.content.Context
import coil.ImageLoader
import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.remote.dto.FeedbackResponseDto
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.RecognitionResult
import com.frootsnoops.brickognize.domain.model.Result
import com.frootsnoops.brickognize.domain.usecase.GetAllBinLocationsUseCase
import com.frootsnoops.brickognize.domain.usecase.SubmitFeedbackUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
class ResultsViewModelFeedbackTest {
    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)
    private lateinit var appContext: Context
    private lateinit var imageLoader: ImageLoader
    
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        appContext = io.mockk.mockk(relaxed = true)
        imageLoader = io.mockk.mockk(relaxed = true)
    }
    
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("Submitting feedback success does not set error")
    fun `submit feedback success`() = runTest(dispatcher) {
        val submitFeedbackUseCase = mockk<SubmitFeedbackUseCase>()
        val getAllBinLocationsUseCase = mockk<GetAllBinLocationsUseCase>()
        val binLocationRepository = mockk<BinLocationRepository>()
        io.mockk.every { getAllBinLocationsUseCase() } returns flowOf(emptyList())
        io.mockk.every { binLocationRepository.getBinLatestPartUpdatesFlow() } returns flowOf(emptyMap())
        coEvery { submitFeedbackUseCase(any(), any(), any(), any(), any(), any()) } returns Result.Success(
            FeedbackResponseDto(status = "ok", message = null)
        )
        val vm = ResultsViewModel(
            appContext = appContext,
            imageLoader = imageLoader,
            assignBinToPartUseCase = mockk(relaxed = true),
            getAllBinLocationsUseCase = getAllBinLocationsUseCase,
            binLocationRepository = binLocationRepository,
            getPartByIdUseCase = mockk(relaxed = true),
            submitFeedbackUseCase = submitFeedbackUseCase,
            appPreferencesRepository = mockk(relaxed = true),
            savedStateHandle = mockk(relaxed = true)
        )
        val item = BrickItem(id = "3001", name = "Brick", type = "part", category = null, imgUrl = null, score = 0.9, binLocation = null)
        val result = RecognitionResult(listingId = "list-1", topCandidate = item, candidates = listOf(item), timestamp = 0L)
        vm.setRecognitionResult(result)
        vm.submitFeedbackForItem(item, true)
        advanceUntilIdle()
        assert(vm.uiState.value.error == null)
    }

    @Test
    @DisplayName("Submitting feedback error sets error state")
    fun `submit feedback error sets error`() = runTest(dispatcher) {
        val submitFeedbackUseCase = mockk<SubmitFeedbackUseCase>()
        val getAllBinLocationsUseCase = mockk<GetAllBinLocationsUseCase>()
        val binLocationRepository = mockk<BinLocationRepository>()
        io.mockk.every { getAllBinLocationsUseCase() } returns flowOf(emptyList())
        io.mockk.every { binLocationRepository.getBinLatestPartUpdatesFlow() } returns flowOf(emptyMap())
        coEvery { submitFeedbackUseCase(any(), any(), any(), any(), any(), any()) } returns Result.Error(Exception("network"), "Failed")
        val vm = ResultsViewModel(
            appContext = appContext,
            imageLoader = imageLoader,
            assignBinToPartUseCase = mockk(relaxed = true),
            getAllBinLocationsUseCase = getAllBinLocationsUseCase,
            binLocationRepository = binLocationRepository,
            getPartByIdUseCase = mockk(relaxed = true),
            submitFeedbackUseCase = submitFeedbackUseCase,
            appPreferencesRepository = mockk(relaxed = true),
            savedStateHandle = mockk(relaxed = true)
        )
        val item = BrickItem(id = "3001", name = "Brick", type = "part", category = null, imgUrl = null, score = 0.9, binLocation = null)
        val result = RecognitionResult(listingId = "list-1", topCandidate = item, candidates = listOf(item), timestamp = 0L)
        vm.setRecognitionResult(result)
        vm.submitFeedbackForItem(item, false)
        advanceUntilIdle()
        assert(vm.uiState.value.error != null)
    }
}
