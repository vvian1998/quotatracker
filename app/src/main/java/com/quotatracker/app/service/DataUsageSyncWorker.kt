package com.quotatracker.app.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.quotatracker.app.data.repository.DataUsageRepository
import com.quotatracker.app.util.Constants
import com.quotatracker.app.util.PermissionUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DataUsageSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val dataUsageRepository: DataUsageRepository
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
            Log.d(tag, "Successfully synced daily usage records to Room DB.")
            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync data usage: ${e.message}", e)
            Result.retry()
        }
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
    }
}
