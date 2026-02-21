package com.frootsnoops.brickognize.ui.scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frootsnoops.brickognize.domain.model.RecognitionResult
import com.frootsnoops.brickognize.domain.model.RecognitionType
import com.frootsnoops.brickognize.domain.model.Result
import com.frootsnoops.brickognize.domain.model.UserError
import com.frootsnoops.brickognize.domain.model.toUserError
import com.frootsnoops.brickognize.domain.usecase.RecognizeImageUseCase
import com.frootsnoops.brickognize.util.NetworkHelper
import com.frootsnoops.brickognize.util.UriFileConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

sealed class ScanUiState {
    data object Idle : ScanUiState()
    data object Capturing : ScanUiState()
    data object Processing : ScanUiState()
    data class Success(val result: RecognitionResult) : ScanUiState()
    data class Error(val error: UserError) : ScanUiState()
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val recognizeImageUseCase: RecognizeImageUseCase,
    private val networkHelper: NetworkHelper,
    private val uriFileConverter: UriFileConverter
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    
    private val _recognitionType = MutableStateFlow(RecognitionType.PARTS)
    val recognitionType: StateFlow<RecognitionType> = _recognitionType.asStateFlow()
    
    private val _autoLaunchCamera = MutableStateFlow(false)
    val autoLaunchCamera: StateFlow<Boolean> = _autoLaunchCamera.asStateFlow()
    
    fun setRecognitionType(type: RecognitionType) {
        Timber.d("Setting recognition type to: ${type.name}")
        _recognitionType.value = type
    }
    
    fun setAutoLaunchCamera(shouldLaunch: Boolean) {
        Timber.d("Setting auto launch camera to: $shouldLaunch")
        _autoLaunchCamera.value = shouldLaunch
    }
    
    fun processImage(imageUri: Uri) {
        Timber.i("Processing image: $imageUri")
        
        if (!networkHelper.isNetworkAvailable()) {
            Timber.w("Network unavailable, cannot process image")
            _uiState.value = ScanUiState.Error(
                UserError.Network(
                    message = "No internet connection. Please check your WiFi or mobile data and try again."
                )
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = ScanUiState.Processing
            
            try {
                // Copy URI to a temporary file
                val imageFile = uriFileConverter.uriToFile(imageUri)
                Timber.d("Image file created: ${imageFile.absolutePath}, size: ${imageFile.length()} bytes")
                
                val result = recognizeImageUseCase(
                    imageFile = imageFile,
                    recognitionType = _recognitionType.value,
                    saveImageLocally = true
                )
                
                when (result) {
                    is Result.Success -> {
                        Timber.i("Image recognition successful: ${result.data.candidates.size} candidates found")
                        _uiState.value = ScanUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Image recognition failed: ${result.message}")
                        val userError = result.toUserError()
                        _uiState.value = ScanUiState.Error(userError)
                    }
                    is Result.Loading -> {
                        // Should not happen
                        Timber.w("Unexpected Loading state from recognizeImageUseCase")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception while processing image")
                val userError = e.toUserError()
                _uiState.value = ScanUiState.Error(userError)
            }
        }
    }
    
    fun resetState() {
        Timber.d("Resetting scan state to Idle")
        _uiState.value = ScanUiState.Idle
        _autoLaunchCamera.value = false
    }
}
