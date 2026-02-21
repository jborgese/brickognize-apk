package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.PartRepository
import javax.inject.Inject

/**
 * Use case for deleting a part by id.
 */
class DeletePartUseCase @Inject constructor(
    private val repository: PartRepository
) {
    suspend operator fun invoke(partId: String) {
        repository.deletePart(partId)
    }
}
