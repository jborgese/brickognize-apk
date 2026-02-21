package com.frootsnoops.brickognize.ui.bins

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.Result
import com.frootsnoops.brickognize.domain.usecase.DeleteBinLocationUseCase
import com.frootsnoops.brickognize.domain.usecase.ExportBinLocationsUseCase
import com.frootsnoops.brickognize.domain.usecase.DeletePartUseCase
import com.frootsnoops.brickognize.domain.usecase.GetAllBinLocationsUseCase
import com.frootsnoops.brickognize.domain.usecase.GetPartsByBinUseCase
import com.frootsnoops.brickognize.domain.usecase.ImportBinLocationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BinWithCount(
    val binLocation: BinLocation,
    val partCount: Int,
    val previewParts: List<BrickItem> = emptyList()
)

data class BinsUiState(
    val bins: List<BinWithCount> = emptyList(),
    val selectedBin: BinLocation? = null,
    val partsInSelectedBin: List<BrickItem> = emptyList(),
    val isLoading: Boolean = false,
    val exportMessage: String? = null,
    val importMessage: String? = null
)

@HiltViewModel
class BinsViewModel @Inject constructor(
    private val getAllBinLocationsUseCase: GetAllBinLocationsUseCase,
    private val getPartsByBinUseCase: GetPartsByBinUseCase,
    private val binLocationRepository: BinLocationRepository,
    private val exportBinLocationsUseCase: ExportBinLocationsUseCase,
    private val importBinLocationsUseCase: ImportBinLocationsUseCase,
    private val deletePartUseCase: DeletePartUseCase,
    private val deleteBinLocationUseCase: DeleteBinLocationUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BinsUiState(isLoading = true))
    val uiState: StateFlow<BinsUiState> = _uiState.asStateFlow()
    private var binsJob: Job? = null
    private var selectionJob: Job? = null
    
    init {
        loadBins()
    }
    
    private fun loadBins() {
        binsJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }
        binsJob = viewModelScope.launch {
            getAllBinLocationsUseCase().collect { bins ->
                // Get part counts and preview parts for each bin
                val binsWithCounts = bins.map { bin ->
                    val count = binLocationRepository.getPartCountForBin(bin.id)
                    val parts = try {
                        // Get the first emission (current value) instead of collecting continuously
                        getPartsByBinUseCase(bin.id).first().take(5)
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to load preview parts for bin ${bin.id}")
                        emptyList()
                    }
                    BinWithCount(bin, count, parts)
                }
                
                _uiState.update {
                    it.copy(
                        bins = binsWithCounts,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun selectBin(binLocation: BinLocation) {
        _uiState.update { it.copy(selectedBin = binLocation) }
        
        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            getPartsByBinUseCase(binLocation.id).collect { parts ->
                _uiState.update { it.copy(partsInSelectedBin = parts) }
            }
        }
    }
    
    fun clearSelection() {
        selectionJob?.cancel()
        _uiState.update { 
            it.copy(
                selectedBin = null,
                partsInSelectedBin = emptyList()
            )
        }
    }
    
    fun refresh() {
        loadBins()
    }
    
    fun exportBinLocations(onExportReady: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = exportBinLocationsUseCase()) {
                is Result.Success -> {
                    Timber.i("Export successful, JSON size: ${result.data.length}")
                    _uiState.update { it.copy(exportMessage = "Export successful") }
                    onExportReady(result.data)
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Export failed: ${result.message}")
                    _uiState.update { it.copy(exportMessage = "Export failed: ${result.message}") }
                }
                is Result.Loading -> {
                    // Not used in this use case
                }
            }
        }
    }
    
    fun importBinLocations(jsonString: String) {
        viewModelScope.launch {
            when (val result = importBinLocationsUseCase(jsonString)) {
                is Result.Success -> {
                    val summary = result.data
                    Timber.i("Import successful: ${summary.binsImported} bins and ${summary.partsImported} parts imported")
                    _uiState.update { 
                        it.copy(
                            importMessage = "Successfully imported ${summary.binsImported} bin location${if (summary.binsImported != 1) "s" else ""} and ${summary.partsImported} part${if (summary.partsImported != 1) "s" else ""}"
                        ) 
                    }
                    loadBins() // Refresh the list
                }
                is Result.Error -> {
                    Timber.e(result.exception, "Import failed: ${result.message}")
                    _uiState.update { it.copy(importMessage = "Import failed: ${result.message}") }
                }
                is Result.Loading -> {
                    // Not used in this use case
                }
            }
        }
    }
    
    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }
    
    fun clearImportMessage() {
        _uiState.update { it.copy(importMessage = null) }
    }
    
    fun deleteBin(binLocation: BinLocation) {
        viewModelScope.launch {
            try {
                Timber.i("Deleting bin: ${binLocation.label} (ID: ${binLocation.id}) and all its parts")
                deleteBinLocationUseCase(binLocation)
                
                // Clear selection if the deleted bin was selected
                if (_uiState.value.selectedBin?.id == binLocation.id) {
                    clearSelection()
                }
                
                // Refresh the bins list
                loadBins()
                Timber.i("Bin and its parts deleted successfully: ${binLocation.label}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete bin: ${binLocation.label}")
                _uiState.update { it.copy(importMessage = "Failed to delete bin: ${e.message}") }
            }
        }
    }

    fun deletePart(part: BrickItem) {
        viewModelScope.launch {
            try {
                Timber.i("Deleting part: ${part.id} from bin ${part.binLocation?.label}")
                deletePartUseCase(part.id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete part: ${part.id}")
                _uiState.update { it.copy(importMessage = "Failed to delete part: ${e.message}") }
            }
        }
    }
}