package com.frootsnoops.brickognize.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frootsnoops.brickognize.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentScansCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getScanHistoryUseCase: GetScanHistoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadRecentScansCount()
    }
    
    private fun loadRecentScansCount() {
        viewModelScope.launch {
            getScanHistoryUseCase(limit = 10).collect { scans ->
                _uiState.update { it.copy(recentScansCount = scans.size) }
            }
        }
    }
}
