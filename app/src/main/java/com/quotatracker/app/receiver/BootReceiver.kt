package com.quotatracker.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.service.DataMonitorService
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
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule WorkManager
            DataUsageSyncWorker.schedule(context)

            // Start foreground monitor service if auto-start is enabled
            CoroutineScope(Dispatchers.IO).launch {
                val autoStart = userPreferences.autoStartOnBootFlow.first()
                if (autoStart) {
                    val serviceIntent = Intent(context, DataMonitorService::class.java)
                    context.startForegroundService(serviceIntent)
                }
            }
        }
    }
}
