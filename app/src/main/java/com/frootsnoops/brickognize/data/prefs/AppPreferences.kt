package com.frootsnoops.brickognize.data.prefs

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object AppPreferencesKeys {
    val ENABLE_LOGGING = booleanPreferencesKey("enable_logging")
    val FEEDBACK_COOLDOWN_SECONDS = intPreferencesKey("feedback_cooldown_seconds")
}

data class AppPreferences(
    val enableLogging: Boolean = true,
    val feedbackCooldownSeconds: Int = 4
)
