package com.frootsnoops.brickognize.di

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
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
            .directory(java.io.File(context.cacheDir, "image_cache").toOkioPath())
            .maxSizeBytes(128L * 1024 * 1024) // 128 MB
            .build()
    }

    @Provides
    @Singleton
    fun provideMemoryCache(): MemoryCache {
        return MemoryCache.Builder()
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
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .diskCache(diskCache)
            .memoryCache(memoryCache)
            .crossfade(true)
            .build()
    }
}
