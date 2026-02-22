package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.Result
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for assigning one or more bin locations to a part.
 * 
 * This use case:
 * 1. Optionally creates a new bin
 * 2. Replaces the part's full bin assignment set
 * 3. Updates the part's updatedAt timestamp
 */
class AssignBinToPartUseCase @Inject constructor(
    private val partRepository: PartRepository,
    private val binLocationRepository: BinLocationRepository
) {
    suspend operator fun invoke(
        partId: String,
        binIds: List<Long> = emptyList(),
        newBinLabel: String? = null,
        newBinDescription: String? = null
    ): Result<Unit> {
        return try {
            Timber.d("AssignBinToPartUseCase: partId=$partId, binIds=$binIds, newLabel=$newBinLabel")

            val normalizedBinIds = binIds.distinct().toMutableSet()
            if (!newBinLabel.isNullOrBlank()) {
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
                
                Timber.i("Creating new bin location: $newBinLabel")
                val createdBinId = binLocationRepository.createBinLocation(newBinLabel, newBinDescription)
                normalizedBinIds += createdBinId
            }

            Timber.i("Updating part $partId with bins ${normalizedBinIds.toList()}")
            partRepository.updatePartBinLocations(partId, normalizedBinIds.toList())
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "AssignBinToPartUseCase: failed to assign bin")
            Result.Error(e, "Failed to assign bin to part: ${e.message}")
        }
    }
}
