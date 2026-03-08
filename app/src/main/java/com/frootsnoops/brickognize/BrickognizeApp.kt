package com.frootsnoops.brickognize

import android.app.Application
import coil.Coil
import coil.ImageLoader
import com.frootsnoops.brickognize.util.CrashReportingTree
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.io.File

@HiltAndroidApp
class BrickognizeApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        initializeTimber()
        initializeCoil()
        cleanStaleCameraImages()
    }
    
    /**
     * Initialize Timber logging.
     * - Debug builds: Log everything to logcat
     * - Release builds: Only log errors (can be sent to crash reporting)
     */
    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            // Debug: Plant debug tree for verbose logging
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber initialized in DEBUG mode")
        } else {
            // Production: Only log errors and send to crash reporting
            Timber.plant(CrashReportingTree())
            Timber.d("Timber initialized in RELEASE mode")
        }
    }

    /**
     * Delete camera temp images older than 24 hours from the cache directory.
     */
    private fun cleanStaleCameraImages() {
        val cameraDir = File(cacheDir, "camera_images")
        if (!cameraDir.exists()) return
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000
        cameraDir.listFiles()?.filter { it.lastModified() < cutoff }?.forEach { file ->
            file.delete()
            Timber.d("Deleted stale camera image: ${file.name}")
        }
    }

    /**
     * Initialize Coil with the app's singleton ImageLoader from Hilt.
     */
    private fun initializeCoil() {
      val entryPoint = EntryPointAccessors.fromApplication(this, ImageLoaderEntryPoint::class.java)
      val imageLoader: ImageLoader = entryPoint.imageLoader()
      Coil.setImageLoader(imageLoader)
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface ImageLoaderEntryPoint {
  fun imageLoader(): ImageLoader
}
