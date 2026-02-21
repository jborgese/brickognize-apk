package com.frootsnoops.brickognize.di

import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.dao.ScanDao
import com.frootsnoops.brickognize.data.remote.api.BrickognizeApi
import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.BrickognizeRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.data.repository.ScanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideBinLocationRepository(
        binLocationDao: BinLocationDao
    ): BinLocationRepository {
        return BinLocationRepository(binLocationDao)
    }
    
    @Provides
    @Singleton
    fun providePartRepository(
        partDao: PartDao,
        binLocationDao: BinLocationDao
    ): PartRepository {
        return PartRepository(partDao, binLocationDao)
    }
    
    @Provides
    @Singleton
    fun provideScanRepository(
        scanDao: ScanDao,
        partDao: PartDao,
        binLocationDao: BinLocationDao
    ): ScanRepository {
        return ScanRepository(scanDao, partDao, binLocationDao)
    }
    
    @Provides
    @Singleton
    fun provideBrickognizeRepository(
        api: BrickognizeApi,
        scanDao: ScanDao,
        partDao: PartDao,
        binLocationDao: BinLocationDao
    ): BrickognizeRepository {
        return BrickognizeRepository(api, scanDao, partDao, binLocationDao)
    }
}
