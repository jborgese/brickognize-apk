package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.BrickItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving parts assigned to a specific bin.
 */
class GetPartsByBinUseCase @Inject constructor(
    private val repository: PartRepository
) {
    operator fun invoke(binLocationId: Long): Flow<List<BrickItem>> {
        return repository.getPartsByBinLocationFlow(binLocationId)
    }
}
