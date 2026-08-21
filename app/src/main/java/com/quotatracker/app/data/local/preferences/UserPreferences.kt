package com.quotatracker.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quotatracker.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.PREFS_NAME)

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_BUBBLE_ENABLED = booleanPreferencesKey("pref_bubble_enabled")
        val KEY_AUTO_START_BOOT = booleanPreferencesKey("pref_auto_start_boot")
        val KEY_WARNING_ENABLED = booleanPreferencesKey("pref_warning_enabled")
        val KEY_WARNING_PERCENT = intPreferencesKey("pref_warning_percent")
        val KEY_QUOTA_CYCLE_DAY = intPreferencesKey("pref_quota_cycle_day")
        val KEY_GLOBAL_QUOTA_BYTES = longPreferencesKey("pref_global_quota_bytes")
        val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("pref_last_sync_timestamp")
    }

    val bubbleEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_BUBBLE_ENABLED] ?: false
    }

    val autoStartOnBootFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTO_START_BOOT] ?: true
    }

    val warningEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WARNING_ENABLED] ?: true
    }

    val warningPercentFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_WARNING_PERCENT] ?: Constants.DEFAULT_WARNING_PERCENT
    }

    val quotaCycleDayFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_QUOTA_CYCLE_DAY] ?: Constants.DEFAULT_CYCLE_DAY
    }

    val globalQuotaBytesFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_GLOBAL_QUOTA_BYTES] ?: Constants.DEFAULT_GLOBAL_QUOTA_BYTES
    }

    val lastSyncTimestampFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_SYNC_TIMESTAMP] ?: 0L
    }

    suspend fun setBubbleEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BUBBLE_ENABLED] = enabled
        }
    }

    suspend fun setAutoStartOnBoot(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_START_BOOT] = enabled
        }
    }

    suspend fun setWarningEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WARNING_ENABLED] = enabled
        }
    }

    suspend fun setWarningPercent(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WARNING_PERCENT] = percent.coerceIn(50, 99)
        }
    }

    suspend fun setQuotaCycleDay(day: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_QUOTA_CYCLE_DAY] = day.coerceIn(1, 28)
        }
    }

    suspend fun setGlobalQuotaBytes(bytes: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GLOBAL_QUOTA_BYTES] = bytes
        }
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
}
