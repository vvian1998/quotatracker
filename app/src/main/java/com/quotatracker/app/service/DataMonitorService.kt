package com.quotatracker.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.quotatracker.app.MainActivity
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.data.repository.DataUsageRepository
import com.quotatracker.app.data.repository.QuotaRepository
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.util.Constants
import com.quotatracker.app.util.DataFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class DataMonitorService : Service() {

    private val tag = "DataMonitorService"
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    @Inject
    lateinit var dataUsageRepository: DataUsageRepository

    @Inject
    lateinit var quotaRepository: QuotaRepository

    @Inject
    lateinit var userPreferences: UserPreferences

    private var hasSentWarning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startAsForeground()
        startMonitoringLoop()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return

        // 1. Ongoing Monitor Channel (LOW priority, silent)
        val monitorChannel = NotificationChannel(
            Constants.CHANNEL_MONITOR_ID,
            Constants.CHANNEL_MONITOR_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Status pemantauan penggunaan kuota data"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(monitorChannel)

        // 2. Quota Warning Channel (HIGH priority)
        val warningChannel = NotificationChannel(
            Constants.CHANNEL_WARNING_ID,
            Constants.CHANNEL_WARNING_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Peringatan saat kuota data hampir habis"
            enableVibration(true)
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(warningChannel)
    }

    private fun startAsForeground() {
        val notification = buildOngoingNotification("Memantau penggunaan data...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.NOTIFICATION_MONITOR_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(Constants.NOTIFICATION_MONITOR_ID, notification)
        }
    }

    private fun buildOngoingNotification(contentText: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.CHANNEL_MONITOR_ID)
            .setContentTitle("QuotaTracker Aktif")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun startMonitoringLoop() {
        serviceScope.launch {
            val notificationManager = getSystemService(NotificationManager::class.java)

            while (isActive) {
                try {
                    val summary = withContext(Dispatchers.IO) {
                        dataUsageRepository.getDeviceSummary(UsagePeriod.MONTHLY).first()
                    }
                    val quota = withContext(Dispatchers.IO) {
                        quotaRepository.getGlobalQuota().first()
                    }

                    val totalUsed = summary.grandTotal
                    val quotaLimit = quota.quotaLimitBytes
                    val warningPercent = quota.warningPercentage

                    // Update ongoing notification
                    val usageText = "${DataFormatter.formatBytes(totalUsed)} / ${DataFormatter.formatBytes(quotaLimit)}"
                    notificationManager?.notify(
                        Constants.NOTIFICATION_MONITOR_ID,
                        buildOngoingNotification("Penggunaan bulan ini: $usageText")
                    )

                    // Check quota warning
                    if (quotaLimit > 0) {
                        val usedRatio = totalUsed.toDouble() / quotaLimit.toDouble()
                        val warningThreshold = warningPercent / 100.0

                        if (usedRatio >= warningThreshold && !hasSentWarning) {
                            hasSentWarning = true
                            sendQuotaWarningNotification(totalUsed, quotaLimit, (usedRatio * 100).toInt())
                        } else if (usedRatio < warningThreshold) {
                            hasSentWarning = false
                        }
                    }

                    // Periodic DB sync
                    dataUsageRepository.syncTodayUsageToDb()

                } catch (e: Exception) {
                    Log.w(tag, "Error in data monitoring loop: ${e.message}")
                }

                delay(60_000L) // Check every 60 seconds
            }
        }
    }

    private fun sendQuotaWarningNotification(used: Long, limit: Long, percent: Int) {
        val notificationManager = getSystemService(NotificationManager::class.java) ?: return

        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_WARNING_ID)
            .setContentTitle("⚠️ Kuota Hampir Habis ($percent%)")
            .setContentText("Anda telah menggunakan ${DataFormatter.formatBytes(used)} dari ${DataFormatter.formatBytes(limit)}.")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(Constants.NOTIFICATION_WARNING_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
