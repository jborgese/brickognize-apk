package com.frootsnoops.brickognize.data.repository

import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.dao.ScanDao
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.relation.ScanWithCandidates
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.ScanHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRepository @Inject constructor(
    private val scanDao: ScanDao,
    private val partDao: PartDao,
    private val binLocationDao: BinLocationDao
) {
    fun getScanHistoryFlow(limit: Int = 50): Flow<List<ScanHistoryItem>> {
        return scanDao.getRecentScansWithCandidatesFlow(limit).map { scansWithCandidates ->
            scansWithCandidates.map { scanWithCandidates ->
                mapToHistoryItem(scanWithCandidates)
            }
        }
    }
    
    private suspend fun mapToHistoryItem(scanWithCandidates: ScanWithCandidates): ScanHistoryItem {
        val scan = scanWithCandidates.scan
        val topItemId = scan.topItemId
        
        val topItem = if (topItemId != null) {
            val part = partDao.getPartById(topItemId)
            val binLocation = part?.binLocationId?.let { binLocationDao.getBinLocationById(it) }
            part?.let {
                BrickItem(
                    id = it.id,
                    name = it.name,
                    type = it.type,
                    category = it.category,
                    imgUrl = it.imgUrl,
                    score = null,
                    binLocation = binLocation?.let { bin ->
                        BinLocation(bin.id, bin.label, bin.description, bin.createdAt)
                    }
                )
            }
        } else {
            null
        }
        
        return ScanHistoryItem(
            scanId = scan.id,
            timestamp = scan.timestamp,
            imagePath = scan.imagePath,
            topItem = topItem,
            candidateCount = scanWithCandidates.candidates.size,
            recognitionType = scan.recognitionType
        )
    }
}
