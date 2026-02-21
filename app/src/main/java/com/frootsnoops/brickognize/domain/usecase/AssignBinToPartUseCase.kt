package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.Result
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for assigning a bin location to a part.
 * 
 * This use case:
 * 1. Creates a new bin if needed (when binId is null and binLabel is provided)
 * 2. Updates the part's binLocationId
 * 3. Updates the part's updatedAt timestamp
 */
class AssignBinToPartUseCase @Inject constructor(
    private val partRepository: PartRepository,
    private val binLocationRepository: BinLocationRepository
) {
    suspend operator fun invoke(
        partId: String,
        binId: Long? = null,
        newBinLabel: String? = null,
        newBinDescription: String? = null
    ): Result<Unit> {
        return try {
            Timber.d("AssignBinToPartUseCase: partId=$partId, binId=$binId, newLabel=$newBinLabel")
            
            val finalBinId = if (binId != null) {
                binId
            } else if (newBinLabel != null) {
                // Check for duplicate bin name before creating
                val existingBins = binLocationRepository.getAllBinLocations()
                val duplicateBin = existingBins.find { 
                    it.label.equals(newBinLabel, ignoreCase = true) 
                }
                
                if (duplicateBin != null) {
                    Timber.w("Bin with label '$newBinLabel' already exists (ID: ${duplicateBin.id})")
                    return Result.Error(
                        Exception("Bin '$newBinLabel' already exists"),
                        "A bin with this name already exists. Please use a different name."
                    )
                }
                
                // Create new bin
                Timber.i("Creating new bin location: $newBinLabel")
                binLocationRepository.createBinLocation(newBinLabel, newBinDescription)
            } else {
                // Clear bin assignment
                Timber.d("Clearing bin assignment for part $partId")
                null
            }
            
            Timber.i("Updating part $partId with bin $finalBinId")
            partRepository.updatePartBinLocation(partId, finalBinId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "AssignBinToPartUseCase: failed to assign bin")
            Result.Error(e, "Failed to assign bin to part: ${e.message}")
        }
    }
}
