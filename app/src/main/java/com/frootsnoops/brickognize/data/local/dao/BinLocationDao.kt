package com.frootsnoops.brickognize.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BinLocationDao {
    
    @Query("SELECT * FROM bin_locations ORDER BY label ASC")
    fun getAllBinLocationsFlow(): Flow<List<BinLocationEntity>>
    
    @Query("SELECT * FROM bin_locations ORDER BY label ASC")
    suspend fun getAllBinLocations(): List<BinLocationEntity>
    
    @Query("SELECT * FROM bin_locations WHERE id = :id")
    suspend fun getBinLocationById(id: Long): BinLocationEntity?
    
    @Query("SELECT * FROM bin_locations WHERE id = :id")
    fun getBinLocationByIdFlow(id: Long): Flow<BinLocationEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBinLocation(binLocation: BinLocationEntity): Long
    
    @Update
    suspend fun updateBinLocation(binLocation: BinLocationEntity)
    
    @Delete
    suspend fun deleteBinLocation(binLocation: BinLocationEntity)
    
    @Query("SELECT COUNT(*) FROM parts WHERE bin_location_id = :binLocationId")
    suspend fun getPartCountForBin(binLocationId: Long): Int
}
