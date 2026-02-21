package com.frootsnoops.brickognize.data.local.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "parts",
    foreignKeys = [
        ForeignKey(
            entity = BinLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["bin_location_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("bin_location_id")]
)
data class PartEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "type")
    val type: String, // "part", "set", "fig"
    
    @ColumnInfo(name = "category")
    val category: String? = null,
    
    @ColumnInfo(name = "img_url")
    val imgUrl: String? = null,
    
    @ColumnInfo(name = "bin_location_id")
    val binLocationId: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
