package com.quotatracker.app.data.repository

import android.util.Log
import com.quotatracker.app.data.local.db.dao.DailyTotalUsage
import com.quotatracker.app.data.local.db.dao.DataUsageDao
import com.quotatracker.app.data.local.db.entity.AppDataUsageEntity
import com.quotatracker.app.data.system.AppUsageHelper
import com.quotatracker.app.data.system.DeviceNetworkSummary
import com.quotatracker.app.data.system.NetworkStatsHelper
import com.quotatracker.app.domain.model.AppDataUsage
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DataUsageRepository(
    private val networkStatsHelper: NetworkStatsHelper,
    private val appUsageHelper: AppUsageHelper,
    private val dataUsageDao: DataUsageDao
) {
    private val tag = "DataUsageRepository"

    /**
     * Get usage list for all apps in the specified period (DAILY, WEEKLY, MONTHLY)
     */
    fun getAppUsageForPeriod(period: UsagePeriod, cycleDay: Int = 1): Flow<List<AppDataUsage>> = flow {
        val (startTime, endTime) = period.getTimeRange(cycleDay)
        val rawList = networkStatsHelper.queryAllAppsUsage(startTime, endTime)

        val totalDeviceBytes = rawList.sumOf { it.grandTotal }

        val appUsageList = rawList.map { raw ->
            val appInfo = appUsageHelper.resolveAppInfo(raw.uid)
            val percentage = if (totalDeviceBytes > 0) {
                (raw.grandTotal.toFloat() / totalDeviceBytes.toFloat())
            } else 0f

            AppDataUsage(
                uid = raw.uid,
                packageName = appInfo.packageName,
                appName = appInfo.appName,
                appIcon = appInfo.icon,
                mobileBytes = raw.totalMobile,
                wifiBytes = raw.totalWifi,
                foregroundBytes = 0L,
                backgroundBytes = 0L,
                totalBytes = raw.grandTotal,
                percentageOfTotal = percentage
            )
        }.sortedByDescending { it.totalBytes }

        emit(appUsageList)
    }.flowOn(Dispatchers.IO)

    /**
     * Get device summary totals (mobile, wifi, download, upload) for period
     */
    fun getDeviceSummary(period: UsagePeriod, cycleDay: Int = 1): Flow<DeviceNetworkSummary> = flow {
        val (startTime, endTime) = period.getTimeRange(cycleDay)
        val summary = networkStatsHelper.queryDeviceSummary(startTime, endTime)
        emit(summary)
    }.flowOn(Dispatchers.IO)

    /**
     * Get detailed breakdown for a specific app UID
     */
    fun getAppDetail(uid: Int, period: UsagePeriod, cycleDay: Int = 1): Flow<AppDataUsage> = flow {
        val (startTime, endTime) = period.getTimeRange(cycleDay)
        val rawDetail = networkStatsHelper.queryAppDetailUsage(uid, startTime, endTime)
        val appInfo = appUsageHelper.resolveAppInfo(uid)

        val detail = AppDataUsage(
            uid = uid,
            packageName = appInfo.packageName,
            appName = appInfo.appName,
            appIcon = appInfo.icon,
            mobileBytes = rawDetail.totalMobile,
            wifiBytes = rawDetail.totalWifi,
            foregroundBytes = rawDetail.totalForeground,
            backgroundBytes = rawDetail.totalBackground,
            totalBytes = rawDetail.grandTotal
        )
        emit(detail)
    }.flowOn(Dispatchers.IO)

    /**
     * Get 7-day daily usage breakdown for a specific app
     */
    fun getAppWeeklyBreakdown(uid: Int): Flow<List<Pair<Long, Long>>> = flow {
        val result = mutableListOf<Pair<Long, Long>>()
        val todayEpochDay = DateUtils.todayEpochDay()

        for (i in 6 downTo 0) {
            val epochDay = todayEpochDay - i
            val startMillis = DateUtils.epochDayToStartMillis(epochDay)
            val endMillis = startMillis + DateUtils.MILLIS_PER_DAY - 1

            val usage = networkStatsHelper.queryAppDetailUsage(uid, startMillis, endMillis)
            result.add(Pair(startMillis, usage.grandTotal))
        }

        emit(result)
    }.flowOn(Dispatchers.IO)

    /**
     * Sync today's snapshot to Room database
     */
    suspend fun syncTodayUsageToDb() = withContext(Dispatchers.IO) {
        try {
            val todayStart = DateUtils.getStartOfToday()
            val now = System.currentTimeMillis()
            val epochDay = DateUtils.todayEpochDay()

            val rawList = networkStatsHelper.queryAllAppsUsage(todayStart, now)
            val entities = rawList.map { raw ->
                val appInfo = appUsageHelper.resolveAppInfo(raw.uid)
                AppDataUsageEntity(
                    uid = raw.uid,
                    packageName = appInfo.packageName,
                    appName = appInfo.appName,
                    dateEpochDay = epochDay,
                    mobileRxBytes = raw.mobileRx,
                    mobileTxBytes = raw.mobileTx,
                    wifiRxBytes = raw.wifiRx,
                    wifiTxBytes = raw.wifiTx,
                    recordedAt = now
                )
            }

            dataUsageDao.insertOrUpdateRecords(entities)
        } catch (e: Exception) {
            Log.e(tag, "Failed to sync data to DB: ${e.message}", e)
        }
    }

    /**
     * Get history daily totals from Room database
     */
    fun getDailyTotals(daysBack: Int = 30): Flow<List<DailyTotalUsage>> {
        val todayEpoch = DateUtils.todayEpochDay()
        val startEpoch = todayEpoch - daysBack
        return dataUsageDao.getDailyTotalsBetween(startEpoch, todayEpoch).flowOn(Dispatchers.IO)
    }

    /**
     * Query today's data usage for a single UID (used by Floating Bubble)
     */
    suspend fun getTodayUsageForUid(uid: Int): Long = withContext(Dispatchers.IO) {
        val todayStart = DateUtils.getStartOfToday()
        val now = System.currentTimeMillis()
        val raw = networkStatsHelper.queryAppDetailUsage(uid, todayStart, now)
        raw.grandTotal
    }
}
