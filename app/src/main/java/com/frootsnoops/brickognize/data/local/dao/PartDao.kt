package com.frootsnoops.brickognize.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.entity.PartBinAssignmentEntity
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PartDao {
    
    @Query("SELECT * FROM parts WHERE id = :id")
    suspend fun getPartById(id: String): PartEntity?
    
    @Query("SELECT * FROM parts WHERE id = :id")
    fun getPartByIdFlow(id: String): Flow<PartEntity?>
    
    @Query(
        """
        SELECT p.*
        FROM parts p
        INNER JOIN part_bin_assignments pba ON pba.part_id = p.id
        WHERE pba.bin_location_id = :binLocationId
        ORDER BY p.name ASC
        """
    )
    fun getPartsByBinLocationFlow(binLocationId: Long): Flow<List<PartEntity>>
    
    @Query(
        """
        SELECT p.*
        FROM parts p
        INNER JOIN part_bin_assignments pba ON pba.part_id = p.id
        WHERE pba.bin_location_id = :binLocationId
        ORDER BY p.name ASC
        """
    )
    suspend fun getPartsByBinLocation(binLocationId: Long): List<PartEntity>

    @Query(
        """
        SELECT b.*
        FROM bin_locations b
        INNER JOIN part_bin_assignments pba ON pba.bin_location_id = b.id
        WHERE pba.part_id = :partId
        ORDER BY b.label ASC
        """
    )
    suspend fun getBinLocationsForPart(partId: String): List<BinLocationEntity>

    @Query(
        """
        SELECT b.*
        FROM bin_locations b
        INNER JOIN part_bin_assignments pba ON pba.bin_location_id = b.id
        WHERE pba.part_id = :partId
        ORDER BY b.label ASC
        """
    )
    fun getBinLocationsForPartFlow(partId: String): Flow<List<BinLocationEntity>>

    @Query("SELECT part_id AS partId, bin_location_id AS binLocationId FROM part_bin_assignments")
    suspend fun getAllPartBinAssignments(): List<PartBinAssignmentRef>

    @Query("SELECT part_id AS partId, bin_location_id AS binLocationId FROM part_bin_assignments")
    fun getAllPartBinAssignmentsFlow(): Flow<List<PartBinAssignmentRef>>
    
    @Query("SELECT * FROM parts ORDER BY updated_at DESC LIMIT :limit")
    fun getRecentPartsFlow(limit: Int = 50): Flow<List<PartEntity>>

    @Query("SELECT * FROM parts")
    suspend fun getAllParts(): List<PartEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPart(part: PartEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertParts(parts: List<PartEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartBinAssignments(assignments: List<PartBinAssignmentEntity>)

    @Query("DELETE FROM part_bin_assignments WHERE part_id = :partId")
    suspend fun deletePartBinAssignments(partId: String)

    @Query("UPDATE parts SET bin_location_id = :primaryBinLocationId, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateLegacyBinLocation(id: String, primaryBinLocationId: Long?, updatedAt: Long)

    @Transaction
    suspend fun replacePartBinAssignments(id: String, binLocationIds: List<Long>, updatedAt: Long) {
        val normalizedIds = binLocationIds.distinct()
        deletePartBinAssignments(id)
        if (normalizedIds.isNotEmpty()) {
            upsertPartBinAssignments(
                normalizedIds.map { binId ->
                    PartBinAssignmentEntity(
                        partId = id,
                        binLocationId = binId,
                        assignedAt = updatedAt
                    )
                }
            )
        }
        updateLegacyBinLocation(
            id = id,
            primaryBinLocationId = normalizedIds.firstOrNull(),
            updatedAt = updatedAt
        )
    }

    @Transaction
    suspend fun updateBinLocation(id: String, binLocationId: Long?, updatedAt: Long) {
        replacePartBinAssignments(id, listOfNotNull(binLocationId), updatedAt)
    }
    
    @Query("DELETE FROM parts WHERE id = :id")
    suspend fun deletePart(id: String)
    
    @Query("SELECT COUNT(*) FROM parts")
    suspend fun getPartCount(): Int
}

data class PartBinAssignmentRef(
    val partId: String,
    val binLocationId: Long
)
