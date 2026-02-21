package com.frootsnoops.brickognize.data.repository

import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.BrickItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartRepository @Inject constructor(
    private val partDao: PartDao,
    private val binLocationDao: BinLocationDao
) {
    suspend fun getPartById(id: String): BrickItem? {
        val part = partDao.getPartById(id) ?: return null
        val binLocation = part.binLocationId?.let { binLocationDao.getBinLocationById(it) }
        return part.toDomainModel(binLocation?.toDomainModel())
    }
    
    fun getPartByIdFlow(id: String): Flow<BrickItem?> {
        return partDao.getPartByIdFlow(id).combine(
            binLocationDao.getAllBinLocationsFlow()
        ) { part, bins ->
            part?.let {
                val binLocation = bins.find { bin -> bin.id == it.binLocationId }
                it.toDomainModel(binLocation?.toDomainModel())
            }
        }
    }
    
    fun getPartsByBinLocationFlow(binLocationId: Long): Flow<List<BrickItem>> {
        return partDao.getPartsByBinLocationFlow(binLocationId).combine(
            binLocationDao.getBinLocationByIdFlow(binLocationId)
        ) { parts, binLocation ->
            parts.map { it.toDomainModel(binLocation?.toDomainModel()) }
        }
    }
    
    suspend fun getPartsByBinLocation(binLocationId: Long): List<BrickItem> {
        val parts = partDao.getPartsByBinLocation(binLocationId)
        val binLocation = binLocationDao.getBinLocationById(binLocationId)
        return parts.map { it.toDomainModel(binLocation?.toDomainModel()) }
    }
    
    suspend fun upsertPart(part: PartEntity) {
        partDao.upsertPart(part)
    }
    
    suspend fun upsertParts(parts: List<PartEntity>) {
        partDao.upsertParts(parts)
    }
    
    suspend fun updatePartBinLocation(partId: String, binLocationId: Long?) {
        partDao.updateBinLocation(partId, binLocationId, System.currentTimeMillis())
    }

    suspend fun deletePart(partId: String) {
        partDao.deletePart(partId)
    }

    // For export: fetch all part entities as-is
    suspend fun getAllPartEntities(): List<PartEntity> {
        return partDao.getAllParts()
    }
    
    private fun PartEntity.toDomainModel(binLocation: BinLocation? = null) = BrickItem(
        id = id,
        name = name,
        type = type,
        category = category,
        imgUrl = imgUrl,
        score = null, // Score is per-scan, not stored on the part itself
        binLocation = binLocation
    )
    
    private fun BinLocationEntity.toDomainModel() = BinLocation(
        id = id,
        label = label,
        description = description,
        createdAt = createdAt
    )
}
