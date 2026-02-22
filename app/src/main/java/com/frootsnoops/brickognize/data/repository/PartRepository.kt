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
        val binLocations = partDao.getBinLocationsForPart(id).map { it.toDomainModel() }
        return part.toDomainModel(binLocations)
    }
    
    fun getPartByIdFlow(id: String): Flow<BrickItem?> {
        return partDao.getPartByIdFlow(id).combine(partDao.getBinLocationsForPartFlow(id)) { part, bins ->
            part?.toDomainModel(bins.map { it.toDomainModel() })
        }
    }
    
    fun getPartsByBinLocationFlow(binLocationId: Long): Flow<List<BrickItem>> {
        return combine(
            partDao.getPartsByBinLocationFlow(binLocationId),
            partDao.getAllPartBinAssignmentsFlow(),
            binLocationDao.getAllBinLocationsFlow()
        ) { parts, assignments, bins ->
            val binEntitiesById = bins.associateBy { it.id }
            val binIdsByPart = assignments
                .groupBy { it.partId }
                .mapValues { (_, refs) -> refs.map { it.binLocationId }.distinct() }
            parts.map { part ->
                val partBinLocations = binIdsByPart[part.id]
                    .orEmpty()
                    .mapNotNull { binId -> binEntitiesById[binId]?.toDomainModel() }
                    .sortedBy { it.label.uppercase() }
                part.toDomainModel(partBinLocations)
            }
        }
    }
    
    suspend fun getPartsByBinLocation(binLocationId: Long): List<BrickItem> {
        val parts = partDao.getPartsByBinLocation(binLocationId)
        if (parts.isEmpty()) {
            return emptyList()
        }
        val binEntitiesById = binLocationDao.getAllBinLocations().associateBy { it.id }
        val binIdsByPart = partDao.getAllPartBinAssignments()
            .groupBy { it.partId }
            .mapValues { (_, refs) -> refs.map { it.binLocationId }.distinct() }

        return parts.map { part ->
            val partBinLocations = binIdsByPart[part.id]
                .orEmpty()
                .mapNotNull { binId -> binEntitiesById[binId]?.toDomainModel() }
                .sortedBy { it.label.uppercase() }
            part.toDomainModel(partBinLocations)
        }
    }
    
    suspend fun upsertPart(part: PartEntity) {
        partDao.upsertPart(part)
    }
    
    suspend fun upsertParts(parts: List<PartEntity>) {
        partDao.upsertParts(parts)
    }
    
    suspend fun updatePartBinLocation(partId: String, binLocationId: Long?) {
        updatePartBinLocations(partId, listOfNotNull(binLocationId))
    }

    suspend fun updatePartBinLocations(
        partId: String,
        binLocationIds: List<Long>,
        updatedAt: Long = System.currentTimeMillis()
    ) {
        partDao.replacePartBinAssignments(
            id = partId,
            binLocationIds = binLocationIds,
            updatedAt = updatedAt
        )
    }

    suspend fun deletePart(partId: String) {
        partDao.deletePart(partId)
    }

    // For export: fetch all part entities as-is
    suspend fun getAllPartEntities(): List<PartEntity> {
        return partDao.getAllParts()
    }

    // For export: map part IDs to all assigned bin IDs
    suspend fun getAllPartBinIds(): Map<String, List<Long>> {
        return partDao.getAllPartBinAssignments()
            .groupBy { it.partId }
            .mapValues { (_, refs) -> refs.map { it.binLocationId }.distinct() }
    }
    
    private fun PartEntity.toDomainModel(binLocations: List<BinLocation> = emptyList()) = BrickItem(
        id = id,
        name = name,
        type = type,
        category = category,
        imgUrl = imgUrl,
        score = null, // Score is per-scan, not stored on the part itself
        binLocation = binLocations.firstOrNull(),
        binLocations = binLocations
    )
    
    private fun BinLocationEntity.toDomainModel() = BinLocation(
        id = id,
        label = label,
        description = description,
        createdAt = createdAt
    )
}
