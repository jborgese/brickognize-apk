package com.frootsnoops.brickognize.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Provides
    @Singleton
    fun provideDiskCache(
        @ApplicationContext context: Context
    ): DiskCache {
        return DiskCache.Builder()
            .directory(java.io.File(context.cacheDir, "image_cache"))
            .maxSizeBytes(128L * 1024 * 1024) // 128 MB
            .build()
    }

    @Provides
    @Singleton
    fun provideMemoryCache(
        @ApplicationContext context: Context
    ): MemoryCache {
        return MemoryCache.Builder(context)
            .maxSizePercent(0.25) // Use up to 25% of app memory
            .build()
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        diskCache: DiskCache,
        memoryCache: MemoryCache
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .diskCache(diskCache)
            .memoryCache(memoryCache)
            .respectCacheHeaders(true)
            .crossfade(true)
            .build()
    }
}
