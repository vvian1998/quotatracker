package com.quotatracker.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.service.DataUsageSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // WorkManager is the background monitor. No foreground service is
        // launched from BOOT_COMPLETED, which is disallowed for dataSync on
        // Android 15 and avoids a detached coroutine race.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (userPreferences.autoStartOnBootFlow.first()) {
                    DataUsageSyncWorker.schedule(context.applicationContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
