package com.frootsnoops.brickognize.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val preferences: Flow<AppPreferences> = dataStore.data.map { prefs ->
        AppPreferences(
            enableLogging = prefs[AppPreferencesKeys.ENABLE_LOGGING] ?: true,
            feedbackCooldownSeconds = prefs[AppPreferencesKeys.FEEDBACK_COOLDOWN_SECONDS] ?: 4
        )
    }

    suspend fun setEnableLogging(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[AppPreferencesKeys.ENABLE_LOGGING] = enabled
        }
    }

    suspend fun setFeedbackCooldownSeconds(seconds: Int) {
        dataStore.edit { prefs ->
            prefs[AppPreferencesKeys.FEEDBACK_COOLDOWN_SECONDS] = seconds
        }
    }
}
