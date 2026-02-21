package com.frootsnoops.brickognize.ui.scan

import android.net.Uri
import app.cash.turbine.test
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.RecognitionResult
import com.frootsnoops.brickognize.domain.model.RecognitionType
import com.frootsnoops.brickognize.domain.model.Result
import com.frootsnoops.brickognize.domain.usecase.RecognizeImageUseCase
import com.frootsnoops.brickognize.util.NetworkHelper
import com.frootsnoops.brickognize.util.UriFileConverter
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("ScanViewModel Tests")
class ScanViewModelTest {

    private lateinit var viewModel: ScanViewModel
    private lateinit var recognizeImageUseCase: RecognizeImageUseCase
    private lateinit var networkHelper: NetworkHelper
    private lateinit var uriFileConverter: UriFileConverter
    private val testDispatcher = StandardTestDispatcher()
    private val mockFile = File("test_image.jpg")

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        recognizeImageUseCase = mockk()
        networkHelper = mockk()
        uriFileConverter = mockk()
        
        viewModel = ScanViewModel(recognizeImageUseCase, networkHelper, uriFileConverter)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    @DisplayName("Initial state should be Idle")
    fun `initial state is idle`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem()).isInstanceOf(ScanUiState.Idle::class.java)
        }
    }

    @Test
    @DisplayName("Recognition type defaults to PARTS")
    fun `default recognition type is PARTS`() = runTest {
        viewModel.recognitionType.test {
            assertThat(awaitItem()).isEqualTo(RecognitionType.PARTS)
        }
    }

    @Test
    @DisplayName("Setting recognition type updates state")
    fun `setRecognitionType updates state`() = runTest {
        viewModel.recognitionType.test {
            assertThat(awaitItem()).isEqualTo(RecognitionType.PARTS)
            
            viewModel.setRecognitionType(RecognitionType.SETS)
            assertThat(awaitItem()).isEqualTo(RecognitionType.SETS)
            
            viewModel.setRecognitionType(RecognitionType.FIGS)
            assertThat(awaitItem()).isEqualTo(RecognitionType.FIGS)
        }
    }

    @Test
    @DisplayName("Process image without network shows error")
    fun `processImage without network shows error`() = runTest {
        every { networkHelper.isNetworkAvailable() } returns false
        val mockUri = mockk<Uri>()

        viewModel.processImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(ScanUiState.Error::class.java)
            val errorState = state as ScanUiState.Error
            assertThat(errorState.error.message).contains("No internet connection")
        }
        
        verify { networkHelper.isNetworkAvailable() }
    }

    @Test
    @DisplayName("Successful image recognition transitions through states correctly")
    fun `successful image recognition transitions states`() = runTest {
        every { networkHelper.isNetworkAvailable() } returns true
        every { uriFileConverter.uriToFile(any()) } returns mockFile
        
        val mockResult = RecognitionResult(
            listingId = "test-123",
            topCandidate = BrickItem(
                id = "part-1",
                name = "Test Part",
                type = "part",
                imgUrl = "https://example.com/part.jpg",
                score = 0.95
            ),
            candidates = listOf(
                BrickItem(
                    id = "part-1",
                    name = "Test Part",
                    type = "part",
                    imgUrl = "https://example.com/part.jpg",
                    score = 0.95
                )
            )
        )
        
        coEvery { 
            recognizeImageUseCase(any(), any(), any()) 
        } returns Result.Success(mockResult)
        
        val mockUri = mockk<Uri>(relaxed = true)

        viewModel.processImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(ScanUiState.Success::class.java)
            
            val successState = state as ScanUiState.Success
            assertThat(successState.result).isEqualTo(mockResult)
            assertThat(successState.result.topCandidate?.name).isEqualTo("Test Part")
        }
        
        coVerify { recognizeImageUseCase(any(), RecognitionType.PARTS, true) }
    }

    @Test
    @DisplayName("Failed image recognition shows error state")
    fun `failed image recognition shows error`() = runTest {
        every { networkHelper.isNetworkAvailable() } returns true
        every { uriFileConverter.uriToFile(any()) } returns mockFile
        
        coEvery { 
            recognizeImageUseCase(any(), any(), any()) 
        } returns Result.Error(Exception("API Error"), "Recognition failed")
        
        val mockUri = mockk<Uri>(relaxed = true)

        viewModel.processImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(ScanUiState.Error::class.java)
            
            val errorState = state as ScanUiState.Error
            assertThat(errorState.error.message).contains("API Error")
        }
    }

    @Test
    @DisplayName("Reset state returns to Idle")
    fun `resetState returns to idle`() = runTest {
        every { networkHelper.isNetworkAvailable() } returns false
        val mockUri = mockk<Uri>()

        viewModel.processImage(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.resetState()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(ScanUiState.Idle::class.java)
        }
    }
}
