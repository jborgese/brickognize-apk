package com.frootsnoops.brickognize.di

import android.content.Context
import androidx.room.Room
import com.frootsnoops.brickognize.BuildConfig
import com.frootsnoops.brickognize.data.local.BrickDatabase
import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.dao.ScanDao
import com.frootsnoops.brickognize.data.local.migrations.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideBrickDatabase(
        @ApplicationContext context: Context
    ): BrickDatabase {
        val builder = Room.databaseBuilder(
            context,
            BrickDatabase::class.java,
            BrickDatabase.DATABASE_NAME
        )
        
        // Add all migrations from centralized location
        // Migrations are defined in DatabaseMigrations.kt
        if (DatabaseMigrations.ALL_MIGRATIONS.isNotEmpty()) {
            builder.addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        }
        
        // ONLY use destructive migration in debug builds during development
        // Production builds REQUIRE proper migrations to preserve user data
        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration()
        }
        // In production (release builds), app will crash if migration is missing
        // This is intentional - better to crash than lose user data!
        
        return builder.build()
    }
    
    @Provides
    @Singleton
    fun provideBinLocationDao(database: BrickDatabase): BinLocationDao {
        return database.binLocationDao()
    }
    
    @Provides
    @Singleton
    fun providePartDao(database: BrickDatabase): PartDao {
        return database.partDao()
    }
    
    @Provides
    @Singleton
    fun provideScanDao(database: BrickDatabase): ScanDao {
        return database.scanDao()
    }
}
