package com.quotatracker.app.data.system

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

data class DeviceNetworkSummary(
    val mobileRx: Long = 0L,
    val mobileTx: Long = 0L,
    val wifiRx: Long = 0L,
    val wifiTx: Long = 0L
) {
    val totalMobile: Long get() = mobileRx + mobileTx
    val totalWifi: Long get() = wifiRx + wifiTx
    val grandTotal: Long get() = totalMobile + totalWifi
    val totalDownload: Long get() = mobileRx + wifiRx
    val totalUpload: Long get() = mobileTx + wifiTx
}

data class RawAppUsage(
    val uid: Int,
    var mobileRx: Long = 0L,
    var mobileTx: Long = 0L,
    var wifiRx: Long = 0L,
    var wifiTx: Long = 0L
) {
    val totalMobile: Long get() = mobileRx + mobileTx
    val totalWifi: Long get() = wifiRx + wifiTx
    val grandTotal: Long get() = totalMobile + totalWifi
}

data class RawAppDetailUsage(
    val uid: Int,
    var mobileRx: Long = 0L,
    var mobileTx: Long = 0L,
    var wifiRx: Long = 0L,
    var wifiTx: Long = 0L,
    var foregroundRx: Long = 0L,
    var foregroundTx: Long = 0L,
    var backgroundRx: Long = 0L,
    var backgroundTx: Long = 0L
) {
    val totalMobile: Long get() = mobileRx + mobileTx
    val totalWifi: Long get() = wifiRx + wifiTx
    val totalForeground: Long get() = foregroundRx + foregroundTx
    val totalBackground: Long get() = backgroundRx + backgroundTx
    val grandTotal: Long get() = totalMobile + totalWifi
}

class NetworkStatsHelper(private val context: Context) {

    private val tag = "NetworkStatsHelper"
    private val networkStatsManager: NetworkStatsManager? by lazy {
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
    }

    /**
     * Query device-wide summary for mobile and wifi
     */
    fun queryDeviceSummary(startTime: Long, endTime: Long): DeviceNetworkSummary {
        val nsm = networkStatsManager ?: return DeviceNetworkSummary()
        var mobileRx = 0L
        var mobileTx = 0L
        var wifiRx = 0L
        var wifiTx = 0L

        // Mobile data
        try {
            val mobileBucket = nsm.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                null,
                startTime,
                endTime
            )
            mobileRx = mobileBucket.rxBytes
            mobileTx = mobileBucket.txBytes
        } catch (e: Exception) {
            Log.w(tag, "Failed to query mobile device summary: ${e.message}")
        }

        // WiFi data
        try {
            val wifiBucket = nsm.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI,
                null,
                startTime,
                endTime
            )
            wifiRx = wifiBucket.rxBytes
            wifiTx = wifiBucket.txBytes
        } catch (e: Exception) {
            Log.w(tag, "Failed to query wifi device summary: ${e.message}")
        }

        return DeviceNetworkSummary(mobileRx, mobileTx, wifiRx, wifiTx)
    }

    /**
     * Query all apps usage across Mobile and WiFi interfaces
     */
    fun queryAllAppsUsage(startTime: Long, endTime: Long): List<RawAppUsage> {
        val nsm = networkStatsManager ?: return emptyList()
        val usageMap = mutableMapOf<Int, RawAppUsage>()

        // 1. Mobile
        queryNetworkTypeSummary(nsm, ConnectivityManager.TYPE_MOBILE, startTime, endTime) { uid, rx, tx ->
            val entry = usageMap.getOrPut(uid) { RawAppUsage(uid) }
            entry.mobileRx += rx
            entry.mobileTx += tx
        }

        // 2. WiFi
        queryNetworkTypeSummary(nsm, ConnectivityManager.TYPE_WIFI, startTime, endTime) { uid, rx, tx ->
            val entry = usageMap.getOrPut(uid) { RawAppUsage(uid) }
            entry.wifiRx += rx
            entry.wifiTx += tx
        }

        return usageMap.values.filter { it.grandTotal > 0 }
    }

    /**
     * Query per-app detailed usage breakdown (mobile vs wifi, foreground vs background)
     */
    fun queryAppDetailUsage(uid: Int, startTime: Long, endTime: Long): RawAppDetailUsage {
        val nsm = networkStatsManager ?: return RawAppDetailUsage(uid)
        val result = RawAppDetailUsage(uid)

        // Mobile detail
        queryDetailsForUidAndType(nsm, ConnectivityManager.TYPE_MOBILE, uid, startTime, endTime) { rx, tx, state ->
            result.mobileRx += rx
            result.mobileTx += tx
            when (state) {
                NetworkStats.Bucket.STATE_FOREGROUND -> {
                    result.foregroundRx += rx
                    result.foregroundTx += tx
                }
                else -> {
                    result.backgroundRx += rx
                    result.backgroundTx += tx
                }
            }
        }

        // WiFi detail
        queryDetailsForUidAndType(nsm, ConnectivityManager.TYPE_WIFI, uid, startTime, endTime) { rx, tx, state ->
            result.wifiRx += rx
            result.wifiTx += tx
            when (state) {
                NetworkStats.Bucket.STATE_FOREGROUND -> {
                    result.foregroundRx += rx
                    result.foregroundTx += tx
                }
                else -> {
                    result.backgroundRx += rx
                    result.backgroundTx += tx
                }
            }
        }

        return result
    }

    private inline fun queryNetworkTypeSummary(
        nsm: NetworkStatsManager,
        networkType: Int,
        startTime: Long,
        endTime: Long,
        crossinline onBucket: (uid: Int, rx: Long, tx: Long) -> Unit
    ) {
        var stats: NetworkStats? = null
        try {
            stats = nsm.querySummary(networkType, null, startTime, endTime)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                if (bucket.uid > 0) {
                    onBucket(bucket.uid, bucket.rxBytes, bucket.txBytes)
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "querySummary error for type $networkType: ${e.message}")
        } finally {
            try {
                stats?.close()
            } catch (e: Exception) {
                // Ignore close error
            }
        }
    }

    private inline fun queryDetailsForUidAndType(
        nsm: NetworkStatsManager,
        networkType: Int,
        uid: Int,
        startTime: Long,
        endTime: Long,
        crossinline onBucket: (rx: Long, tx: Long, state: Int) -> Unit
    ) {
        var stats: NetworkStats? = null
        try {
            stats = nsm.queryDetailsForUid(networkType, null, startTime, endTime, uid)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                onBucket(bucket.rxBytes, bucket.txBytes, bucket.state)
            }
        } catch (e: Exception) {
            Log.w(tag, "queryDetailsForUid error for uid $uid, type $networkType: ${e.message}")
        } finally {
            try {
                stats?.close()
            } catch (e: Exception) {
                // Ignore close error
            }
        }
    }
}
