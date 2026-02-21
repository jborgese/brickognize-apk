package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.BrickItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a single part by its ID with bin location info.
 */
class GetPartByIdUseCase @Inject constructor(
    private val repository: PartRepository
) {
    operator fun invoke(partId: String): Flow<BrickItem?> {
        return repository.getPartByIdFlow(partId)
    }
    
    suspend fun getOnce(partId: String): BrickItem? {
        return repository.getPartById(partId)
    }
}
