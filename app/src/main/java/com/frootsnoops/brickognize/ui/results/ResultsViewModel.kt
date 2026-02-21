package com.frootsnoops.brickognize.ui.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.RecognitionResult
import com.frootsnoops.brickognize.domain.model.UserError
import com.frootsnoops.brickognize.domain.model.toUserError
import com.frootsnoops.brickognize.data.prefs.AppPreferencesRepository
import com.frootsnoops.brickognize.domain.usecase.AssignBinToPartUseCase
import com.frootsnoops.brickognize.domain.usecase.GetAllBinLocationsUseCase
import com.frootsnoops.brickognize.domain.usecase.GetPartByIdUseCase
import com.frootsnoops.brickognize.domain.usecase.SubmitFeedbackUseCase
import android.content.Context
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ResultsUiState(
    val recognitionResult: RecognitionResult? = null,
    val availableBins: List<BinLocation> = emptyList(),
    val showBinPicker: Boolean = false,
    val selectedPartId: String? = null,
    val isAssigningBin: Boolean = false,
    val error: UserError? = null,
    val feedbackMessage: String? = null,
    val feedbackCooldownUntil: Long? = null
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val imageLoader: ImageLoader,
    private val assignBinToPartUseCase: AssignBinToPartUseCase,
    private val getAllBinLocationsUseCase: GetAllBinLocationsUseCase,
    private val getPartByIdUseCase: GetPartByIdUseCase,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val appPreferencesRepository: AppPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
        private val recentFeedback = mutableMapOf<String, Long>() // key: listingId|itemId
        private var debounceMillis = 4000L
    
    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()
    
    init {
        loadBinLocations()
        // Observe preferences to allow runtime tweak of cooldown
        viewModelScope.launch {
            appPreferencesRepository.preferences.collect { prefs ->
                debounceMillis = (prefs.feedbackCooldownSeconds * 1000L).coerceAtLeast(1000L)
                Timber.d("Updated feedback debounce to ${debounceMillis}ms from preferences")
            }
        }
    }
    
    fun setRecognitionResult(result: RecognitionResult) {
        Timber.d("Setting recognition result: ${result.candidates.size} candidates")
        _uiState.update { it.copy(recognitionResult = result) }
        // Prefetch candidate images to warm caches (non-blocking)
        prefetchCandidateImages(result.candidates.mapNotNull { it.imgUrl })
    }
    
    fun showBinPicker(partId: String) {
        Timber.d("Showing bin picker for part: $partId")
        _uiState.update { 
            it.copy(
                showBinPicker = true,
                selectedPartId = partId
            )
        }
    }

    /**
     * Prefetch candidate images so they render smoothly when bound.
     */
    fun prefetchCandidateImages(urls: List<String>, limit: Int = 12) {
        val toPrefetch = urls.take(limit)
        if (toPrefetch.isEmpty()) return
        viewModelScope.launch {
            toPrefetch.forEach { url ->
                val request = ImageRequest.Builder(appContext)
                    .data(url)
                    .build()
                imageLoader.enqueue(request)
            }
        }
    }
    
    fun hideBinPicker() {
        Timber.d("Hiding bin picker")
        _uiState.update { 
            it.copy(
                showBinPicker = false,
                selectedPartId = null
            )
        }
    }
    
    fun assignBinToPart(binId: Long?, newBinLabel: String? = null, newBinDescription: String? = null) {
        val partId = _uiState.value.selectedPartId ?: run {
            Timber.w("assignBinToPart called with no selected part")
            return
        }
        
        Timber.i("Assigning bin to part: partId=$partId, binId=$binId, newBinLabel=$newBinLabel")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isAssigningBin = true) }
            
            val result = assignBinToPartUseCase(
                partId = partId,
                binId = binId,
                newBinLabel = newBinLabel,
                newBinDescription = newBinDescription
            )
            
            when (result) {
                is com.frootsnoops.brickognize.domain.model.Result.Success -> {
                    Timber.i("Bin assigned successfully to part: $partId")
                    // Refresh bin locations
                    loadBinLocations()
                    
                    // Update the recognition result with new bin assignment
                    refreshPartBinLocation(partId)
                    
                    _uiState.update { 
                        it.copy(
                            isAssigningBin = false,
                            showBinPicker = false,
                            selectedPartId = null
                        )
                    }
                }
                is com.frootsnoops.brickognize.domain.model.Result.Error -> {
                    Timber.e(result.exception, "Failed to assign bin to part: ${result.message}")
                    val userError = result.toUserError()
                    _uiState.update { 
                        it.copy(
                            isAssigningBin = false,
                            error = userError
                        )
                    }
                }
                else -> {}
            }
        }
    }
    
    fun clearError() {
        Timber.d("Clearing error")
        _uiState.update { it.copy(error = null) }
    }
    
    private fun loadBinLocations() {
        Timber.d("Loading bin locations")
        viewModelScope.launch {
            getAllBinLocationsUseCase().collect { bins ->
                Timber.d("Loaded ${bins.size} bin locations")
                _uiState.update { it.copy(availableBins = bins) }
            }
        }
    }
    
    private fun refreshPartBinLocation(partId: String) {
        Timber.d("Refreshing part bin location for: $partId")
        viewModelScope.launch {
            val updatedPart = getPartByIdUseCase.getOnce(partId)
            val currentResult = _uiState.value.recognitionResult
            
            if (updatedPart != null && currentResult != null) {
                // Update the part in the candidates list
                val updatedCandidates = currentResult.candidates.map { candidate ->
                    if (candidate.id == partId) {
                        candidate.copy(binLocation = updatedPart.binLocation)
                    } else {
                        candidate
                    }
                }
                
                // Update the top candidate if it matches
                val updatedTopCandidate = if (currentResult.topCandidate?.id == partId) {
                    currentResult.topCandidate.copy(binLocation = updatedPart.binLocation)
                } else {
                    currentResult.topCandidate
                }
                
                val updatedResult = currentResult.copy(
                    candidates = updatedCandidates,
                    topCandidate = updatedTopCandidate
                )
                
                _uiState.update { it.copy(recognitionResult = updatedResult) }
            }
        }
    }

    fun submitFeedbackForItem(item: BrickItem, isCorrect: Boolean) {
        val result = _uiState.value.recognitionResult ?: run {
            Timber.w("submitFeedbackForItem called with no recognition result")
            return
        }
        val listingId = result.listingId
        val itemType = item.type
        val itemRank = result.candidates.indexOfFirst { it.id == item.id }.takeIf { it >= 0 } ?: 0
        val key = "$listingId|${item.id}"
        val now = System.currentTimeMillis()
        val last = recentFeedback[key]
        if (last != null && now - last < debounceMillis) {
            Timber.d("Feedback debounced for $key")
            return
        }
        recentFeedback[key] = now
        // Start a global cooldown on feedback actions
        _uiState.update { it.copy(feedbackCooldownUntil = now + debounceMillis) }
        Timber.i("Submitting feedback for item ${item.id}: correct=$isCorrect, rank=$itemRank")
        viewModelScope.launch {
            when (val resp = submitFeedbackUseCase(
                listingId = listingId,
                itemId = item.id,
                itemType = itemType,
                itemRank = itemRank,
                isPredictionCorrect = isCorrect
            )) {
                is com.frootsnoops.brickognize.domain.model.Result.Success -> {
                    Timber.i("Feedback submitted successfully: ${resp.data.status}")
                    _uiState.update { it.copy(feedbackMessage = "Thanks for your feedback") }
                }
                is com.frootsnoops.brickognize.domain.model.Result.Error -> {
                    Timber.e(resp.exception, "Failed to submit feedback: ${resp.message}")
                    val userError = resp.toUserError()
                    _uiState.update { it.copy(error = userError) }
                }
                else -> {}
            }
        }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }
}
