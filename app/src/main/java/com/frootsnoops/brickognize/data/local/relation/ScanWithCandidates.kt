package com.frootsnoops.brickognize.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.frootsnoops.brickognize.data.local.entity.ScanCandidateEntity
import com.frootsnoops.brickognize.data.local.entity.ScanEntity

/**
 * Relation model that combines a scan with its candidate items.
 */
data class ScanWithCandidates(
    @Embedded val scan: ScanEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "scan_id"
    )
    val candidates: List<ScanCandidateEntity>
)
