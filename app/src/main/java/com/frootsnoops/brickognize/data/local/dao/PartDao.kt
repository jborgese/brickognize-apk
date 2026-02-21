package com.frootsnoops.brickognize.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartDao {
    
    @Query("SELECT * FROM parts WHERE id = :id")
    suspend fun getPartById(id: String): PartEntity?
    
    @Query("SELECT * FROM parts WHERE id = :id")
    fun getPartByIdFlow(id: String): Flow<PartEntity?>
    
    @Query("SELECT * FROM parts WHERE bin_location_id = :binLocationId ORDER BY name ASC")
    fun getPartsByBinLocationFlow(binLocationId: Long): Flow<List<PartEntity>>
    
    @Query("SELECT * FROM parts WHERE bin_location_id = :binLocationId ORDER BY name ASC")
    suspend fun getPartsByBinLocation(binLocationId: Long): List<PartEntity>
    
    @Query("SELECT * FROM parts ORDER BY updated_at DESC LIMIT :limit")
    fun getRecentPartsFlow(limit: Int = 50): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts")
    suspend fun getAllParts(): List<PartEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPart(part: PartEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParts(parts: List<PartEntity>)
    
    @Query("UPDATE parts SET bin_location_id = :binLocationId, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateBinLocation(id: String, binLocationId: Long?, updatedAt: Long)
    
    @Query("DELETE FROM parts WHERE id = :id")
    suspend fun deletePart(id: String)
    
    @Query("SELECT COUNT(*) FROM parts")
    suspend fun getPartCount(): Int
}
