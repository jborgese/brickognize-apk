package com.frootsnoops.brickognize.data.local.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "scans",
    indices = [Index("timestamp")]
)
data class ScanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "image_path")
    val imagePath: String? = null,
    
    @ColumnInfo(name = "listing_id")
    val listingId: String? = null,
    
    @ColumnInfo(name = "top_item_id")
    val topItemId: String? = null,
    
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    
    @ColumnInfo(name = "recognition_type")
    val recognitionType: String = "parts" // "parts", "sets", "figs"
)
