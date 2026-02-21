package com.frootsnoops.brickognize.util

import android.util.Log
import timber.log.Timber

/**
 * Custom Timber tree for production builds.
 * 
 * Only logs errors and warnings (no debug/info logs in production).
 * This can be extended to send crash reports to services like Firebase Crashlytics.
 * 
 * Usage:
 * ```
 * if (!BuildConfig.DEBUG) {
 *     Timber.plant(CrashReportingTree())
 * }
 * ```
 */
class CrashReportingTree : Timber.Tree() {
    
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only log warnings, errors, and asserts in production
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }
        
        // Log to Android logcat (for release builds connected via adb)
        if (t != null) {
            Log.println(priority, tag, "$message\n${Log.getStackTraceString(t)}")
        } else {
            Log.println(priority, tag, message)
        }
        
        // TODO: Send to crash reporting service (e.g., Firebase Crashlytics)
        // Example:
        // if (priority == Log.ERROR || priority == Log.ASSERT) {
        //     if (t != null) {
        //         FirebaseCrashlytics.getInstance().recordException(t)
        //     } else {
        //         FirebaseCrashlytics.getInstance().log("$tag: $message")
        //     }
        // }
    }
}
