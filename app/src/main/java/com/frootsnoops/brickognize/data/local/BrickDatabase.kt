package com.frootsnoops.brickognize.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.dao.ScanDao
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.entity.PartBinAssignmentEntity
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.frootsnoops.brickognize.data.local.entity.ScanCandidateEntity
import com.frootsnoops.brickognize.data.local.entity.ScanEntity

@Database(
    entities = [
        BinLocationEntity::class,
        PartEntity::class,
        PartBinAssignmentEntity::class,
        ScanEntity::class,
        ScanCandidateEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class BrickDatabase : RoomDatabase() {
    abstract fun binLocationDao(): BinLocationDao
    abstract fun partDao(): PartDao
    abstract fun scanDao(): ScanDao
    
    companion object {
        const val DATABASE_NAME = "brick_database"
    }
}
