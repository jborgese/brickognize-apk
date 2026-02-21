package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for deleting a bin location along with all parts assigned to it
 */
class DeleteBinLocationUseCase @Inject constructor(
    private val binLocationRepository: BinLocationRepository,
    private val partRepository: PartRepository
) {
    suspend operator fun invoke(binLocation: BinLocation) {
        Timber.i("DeleteBinLocationUseCase: Deleting bin ${binLocation.label} (ID: ${binLocation.id}) and all its parts")
        
        // First, get all parts in this bin
        val partsInBin = partRepository.getPartsByBinLocation(binLocation.id)
        Timber.i("Found ${partsInBin.size} parts to delete in bin ${binLocation.label}")
        
        // Delete all parts in the bin
        partsInBin.forEach { part ->
            Timber.d("Deleting part ${part.id} from bin ${binLocation.label}")
            partRepository.deletePart(part.id)
        }
        
        // Then delete the bin itself
        binLocationRepository.deleteBinLocation(binLocation)
        Timber.i("Bin ${binLocation.label} and all its parts deleted successfully")
    }
}
