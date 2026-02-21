package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all bin locations.
 */
class GetAllBinLocationsUseCase @Inject constructor(
    private val repository: BinLocationRepository
) {
    operator fun invoke(): Flow<List<BinLocation>> {
        return repository.getAllBinLocationsFlow()
    }
}
