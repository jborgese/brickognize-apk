package com.frootsnoops.brickognize.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.frootsnoops.brickognize.data.local.entity.ScanCandidateEntity
import com.frootsnoops.brickognize.data.local.entity.ScanEntity
import com.frootsnoops.brickognize.data.local.relation.ScanWithCandidates
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    
    @Transaction
    @Query("SELECT * FROM scans ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentScansWithCandidatesFlow(limit: Int = 50): Flow<List<ScanWithCandidates>>
    
    @Transaction
    @Query("SELECT * FROM scans WHERE id = :scanId")
    suspend fun getScanWithCandidates(scanId: Long): ScanWithCandidates?
    
    @Transaction
    @Query("SELECT * FROM scans WHERE id = :scanId")
    fun getScanWithCandidatesFlow(scanId: Long): Flow<ScanWithCandidates?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCandidates(candidates: List<ScanCandidateEntity>)
    
    @Transaction
    suspend fun insertScanWithCandidates(scan: ScanEntity, candidates: List<ScanCandidateEntity>): Long {
        val scanId = insertScan(scan)
        val candidatesWithScanId = candidates.map { it.copy(scanId = scanId) }
        insertCandidates(candidatesWithScanId)
        return scanId
    }
    
    @Query("DELETE FROM scans WHERE id = :scanId")
    suspend fun deleteScan(scanId: Long)
    
    @Query("SELECT COUNT(*) FROM scans")
    suspend fun getScanCount(): Int
}
