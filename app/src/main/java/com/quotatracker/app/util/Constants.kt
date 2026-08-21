package com.quotatracker.app.util

object Constants {
    // Notification Channels
    const val CHANNEL_MONITOR_ID = "channel_data_monitor"
    const val CHANNEL_MONITOR_NAME = "Pemantau Kuota"
    const val NOTIFICATION_MONITOR_ID = 1001

    const val CHANNEL_WARNING_ID = "channel_quota_warning"
    const val CHANNEL_WARNING_NAME = "Peringatan Kuota"
    const val NOTIFICATION_WARNING_ID = 1002

    const val CHANNEL_BUBBLE_ID = "channel_floating_bubble"
    const val CHANNEL_BUBBLE_NAME = "Floating Bubble"
    const val NOTIFICATION_BUBBLE_ID = 1003

    // Default Quota Settings
    const val DEFAULT_GLOBAL_QUOTA_BYTES = 5L * 1024 * 1024 * 1024 // 5 GB
    const val DEFAULT_WARNING_PERCENT = 80 // 80%
    const val DEFAULT_CYCLE_DAY = 1

    // Polling Intervals
    const val SPEED_MONITOR_INTERVAL_MS = 1000L
    const val FOREGROUND_POLL_INTERVAL_MS = 4000L
    const val DATA_USAGE_SYNC_INTERVAL_MIN = 15L

    // Intent Actions & Extras
    const val ACTION_START_BUBBLE = "com.quotatracker.app.action.START_BUBBLE"
    const val ACTION_STOP_BUBBLE = "com.quotatracker.app.action.STOP_BUBBLE"
    const val EXTRA_APP_UID = "extra_app_uid"
    const val EXTRA_PACKAGE_NAME = "extra_package_name"

    // DataStore Keys
    const val PREFS_NAME = "quota_tracker_prefs"
}
