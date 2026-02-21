package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.ScanRepository
import com.frootsnoops.brickognize.domain.model.ScanHistoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving scan history.
 */
class GetScanHistoryUseCase @Inject constructor(
    private val repository: ScanRepository
) {
    operator fun invoke(limit: Int = 50): Flow<List<ScanHistoryItem>> {
        return repository.getScanHistoryFlow(limit)
    }
}
