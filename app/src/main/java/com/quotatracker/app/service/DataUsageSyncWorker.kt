package com.quotatracker.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.quotatracker.app.MainActivity
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.data.repository.DataUsageRepository
import com.quotatracker.app.data.repository.QuotaRepository
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.util.Constants
import com.quotatracker.app.util.DataFormatter
import com.quotatracker.app.util.PermissionUtils
import com.quotatracker.app.util.QuotaRules
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class DataUsageSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataUsageRepository: DataUsageRepository,
    private val quotaRepository: QuotaRepository,
    private val userPreferences: UserPreferences
) : CoroutineWorker(appContext, workerParams) {

    private val tag = "DataUsageSyncWorker"

    override suspend fun doWork(): Result {
        if (!PermissionUtils.hasUsageStatsPermission(applicationContext)) {
            Log.w(tag, "Usage stats permission not granted. Skipping sync.")
            return Result.failure()
        }

        return try {
            dataUsageRepository.syncTodayUsageToDb()
            dataUsageRepository.pruneOldHistory()
            evaluateQuotaWarning()
            userPreferences.setLastSyncTimestamp(System.currentTimeMillis())
            Log.d(tag, "Successfully synced daily usage records to Room DB.")
            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync data usage: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun evaluateQuotaWarning() {
        val warningEnabled = userPreferences.warningEnabledFlow.first()
        val quota = quotaRepository.getGlobalQuota().first()
        val cycleDay = userPreferences.quotaCycleDayFlow.first()
        val summary = dataUsageRepository
            .getDeviceSummary(UsagePeriod.MONTHLY, cycleDay)
            .first()

        val shouldWarn = QuotaRules.shouldWarn(
            usedBytes = summary.grandTotal,
            quotaBytes = quota.quotaLimitBytes,
            warningPercent = quota.warningPercentage,
            warningEnabled = warningEnabled
        )
        val warningWasActive = userPreferences.warningActiveFlow.first()

        if (!shouldWarn) {
            if (warningWasActive) userPreferences.setWarningActive(false)
            return
        }

        if (!warningWasActive && PermissionUtils.hasNotificationPermission(applicationContext)) {
            sendQuotaWarning(
                used = summary.grandTotal,
                limit = quota.quotaLimitBytes,
                percent = ((summary.grandTotal.toDouble() / quota.quotaLimitBytes) * 100).toInt()
            )
            userPreferences.setWarningActive(true)
        }
    }

    private fun sendQuotaWarning(used: Long, limit: Long, percent: Int) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    Constants.CHANNEL_WARNING_ID,
                    Constants.CHANNEL_WARNING_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Peringatan saat kuota data hampir habis"
                    enableVibration(true)
                    setShowBadge(true)
                }
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(
            applicationContext,
            Constants.CHANNEL_WARNING_ID
        )
            .setContentTitle("⚠️ Kuota Hampir Habis (${percent}%)")
            .setContentText(
                "Anda telah menggunakan ${DataFormatter.formatBytes(used)} " +
                    "dari ${DataFormatter.formatBytes(limit)}."
            )
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(Constants.NOTIFICATION_WARNING_ID, notification)
    }

    companion object {
        private const val WORK_NAME = "PeriodicDataUsageSyncWork"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<DataUsageSyncWorker>(
                Constants.DATA_USAGE_SYNC_INTERVAL_MIN,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
