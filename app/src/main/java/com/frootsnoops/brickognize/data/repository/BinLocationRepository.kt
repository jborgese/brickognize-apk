package com.frootsnoops.brickognize.data.repository

import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.domain.model.BinLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinLocationRepository @Inject constructor(
    private val binLocationDao: BinLocationDao
) {
    fun getAllBinLocationsFlow(): Flow<List<BinLocation>> {
        return binLocationDao.getAllBinLocationsFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun getAllBinLocations(): List<BinLocation> {
        return binLocationDao.getAllBinLocations().map { it.toDomainModel() }
    }
    
    suspend fun getBinLocationById(id: Long): BinLocation? {
        return binLocationDao.getBinLocationById(id)?.toDomainModel()
    }
    
    suspend fun createBinLocation(label: String, description: String? = null): Long {
        val entity = BinLocationEntity(
            label = label,
            description = description
        )
        return binLocationDao.insertBinLocation(entity)
    }
    
    suspend fun updateBinLocation(binLocation: BinLocation) {
        val entity = BinLocationEntity(
            id = binLocation.id,
            label = binLocation.label,
            description = binLocation.description,
            createdAt = binLocation.createdAt
        )
        binLocationDao.updateBinLocation(entity)
    }
    
    suspend fun deleteBinLocation(binLocation: BinLocation) {
        val entity = BinLocationEntity(
            id = binLocation.id,
            label = binLocation.label,
            description = binLocation.description,
            createdAt = binLocation.createdAt
        )
        binLocationDao.deleteBinLocation(entity)
    }
    
    suspend fun getPartCountForBin(binLocationId: Long): Int {
        return binLocationDao.getPartCountForBin(binLocationId)
    }
    
    private fun BinLocationEntity.toDomainModel() = BinLocation(
        id = id,
        label = label,
        description = description,
        createdAt = createdAt
    )
}
