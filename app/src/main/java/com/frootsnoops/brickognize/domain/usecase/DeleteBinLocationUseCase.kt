package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for deleting a bin location.
 *
 * Part assignments are removed automatically via foreign key cascade from
 * part_bin_assignments, while parts themselves are preserved.
 */
class DeleteBinLocationUseCase @Inject constructor(
    private val binLocationRepository: BinLocationRepository
) {
    suspend operator fun invoke(binLocation: BinLocation) {
        Timber.i("DeleteBinLocationUseCase: Deleting bin ${binLocation.label} (ID: ${binLocation.id})")
        binLocationRepository.deleteBinLocation(binLocation)
        Timber.i("Bin ${binLocation.label} deleted successfully")
    }
}
