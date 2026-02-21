package com.frootsnoops.brickognize.data.local.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Keep
@Entity(
    tableName = "scan_candidates",
    primaryKeys = ["scan_id", "item_id"],
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scan_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PartEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scan_id"), Index("item_id")]
)
data class ScanCandidateEntity(
    @ColumnInfo(name = "scan_id")
    val scanId: Long,
    
    @ColumnInfo(name = "item_id")
    val itemId: String,
    
    @ColumnInfo(name = "rank")
    val rank: Int,
    
    @ColumnInfo(name = "score")
    val score: Double
)
