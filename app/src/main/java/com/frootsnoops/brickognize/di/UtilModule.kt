package com.frootsnoops.brickognize.di

import com.frootsnoops.brickognize.util.UriFileConverter
import com.frootsnoops.brickognize.util.UriFileConverterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilModule {
    
    @Binds
    @Singleton
    abstract fun bindUriFileConverter(
        impl: UriFileConverterImpl
    ): UriFileConverter
}
