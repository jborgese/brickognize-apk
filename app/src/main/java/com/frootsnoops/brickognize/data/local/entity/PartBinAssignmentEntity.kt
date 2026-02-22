package com.frootsnoops.brickognize.data.local.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Keep
@Entity(
    tableName = "part_bin_assignments",
    primaryKeys = ["part_id", "bin_location_id"],
    foreignKeys = [
        ForeignKey(
            entity = PartEntity::class,
            parentColumns = ["id"],
            childColumns = ["part_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BinLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["bin_location_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("part_id"),
        Index("bin_location_id")
    ]
)
data class PartBinAssignmentEntity(
    @ColumnInfo(name = "part_id")
    val partId: String,

    @ColumnInfo(name = "bin_location_id")
    val binLocationId: Long,

    @ColumnInfo(name = "assigned_at")
    val assignedAt: Long = System.currentTimeMillis()
)
