package com.frootsnoops.brickognize.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frootsnoops.brickognize.domain.model.ScanHistoryItem
import com.frootsnoops.brickognize.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val historyItems: List<ScanHistoryItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getScanHistoryUseCase: GetScanHistoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    init {
        loadHistory()
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            getScanHistoryUseCase(limit = 50).collect { items ->
                _uiState.value = HistoryUiState(
                    historyItems = items,
                    isLoading = false
                )
            }
        }
    }
    
    fun refresh() {
        loadHistory()
    }
}
