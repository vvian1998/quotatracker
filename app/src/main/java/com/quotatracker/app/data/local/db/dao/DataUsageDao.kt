package com.quotatracker.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quotatracker.app.data.local.db.entity.AppDataUsageEntity
import kotlinx.coroutines.flow.Flow

data class AggregatedAppUsage(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val totalMobileRx: Long,
    val totalMobileTx: Long,
    val totalWifiRx: Long,
    val totalWifiTx: Long,
    val totalForeground: Long,
    val totalBackground: Long
) {
    val totalMobile: Long get() = totalMobileRx + totalMobileTx
    val totalWifi: Long get() = totalWifiRx + totalWifiTx
    val grandTotal: Long get() = totalMobile + totalWifi
}

data class DailyTotalUsage(
    val dateEpochDay: Long,
    val totalMobile: Long,
    val totalWifi: Long,
    val grandTotal: Long
)

@Dao
interface DataUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecords(records: List<AppDataUsageEntity>)

    @Query("""
        SELECT * FROM app_data_usage 
        WHERE dateEpochDay = :epochDay 
        ORDER BY (mobileRxBytes + mobileTxBytes + wifiRxBytes + wifiTxBytes) DESC
    """)
    fun getDailyUsageFlow(epochDay: Long): Flow<List<AppDataUsageEntity>>

    @Query("""
        SELECT 
            uid,
            packageName,
            appName,
            SUM(mobileRxBytes) as totalMobileRx,
            SUM(mobileTxBytes) as totalMobileTx,
            SUM(wifiRxBytes) as totalWifiRx,
            SUM(wifiTxBytes) as totalWifiTx,
            SUM(foregroundBytes) as totalForeground,
            SUM(backgroundBytes) as totalBackground
        FROM app_data_usage
        WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY uid
        ORDER BY (totalMobileRx + totalMobileTx + totalWifiRx + totalWifiTx) DESC
    """)
    fun getAggregatedUsageBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<AggregatedAppUsage>>

    @Query("""
        SELECT 
            dateEpochDay,
            SUM(mobileRxBytes + mobileTxBytes) as totalMobile,
            SUM(wifiRxBytes + wifiTxBytes) as totalWifi,
            SUM(mobileRxBytes + mobileTxBytes + wifiRxBytes + wifiTxBytes) as grandTotal
        FROM app_data_usage
        WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY dateEpochDay
        ORDER BY dateEpochDay DESC
    """)
    fun getDailyTotalsBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyTotalUsage>>

    @Query("""
        SELECT 
            uid,
            packageName,
            appName,
            SUM(mobileRxBytes) as totalMobileRx,
            SUM(mobileTxBytes) as totalMobileTx,
            SUM(wifiRxBytes) as totalWifiRx,
            SUM(wifiTxBytes) as totalWifiTx,
            SUM(foregroundBytes) as totalForeground,
            SUM(backgroundBytes) as totalBackground
        FROM app_data_usage
        WHERE uid = :uid AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        GROUP BY uid
    """)
    fun getAppUsageDetailBetween(uid: Int, startEpochDay: Long, endEpochDay: Long): Flow<AggregatedAppUsage?>

    @Query("""
        SELECT * FROM app_data_usage
        WHERE uid = :uid AND dateEpochDay BETWEEN :startEpochDay AND :endEpochDay
        ORDER BY dateEpochDay ASC
    """)
    fun getAppDailyRecords(uid: Int, startEpochDay: Long, endEpochDay: Long): Flow<List<AppDataUsageEntity>>

    @Query("""
        DELETE FROM app_data_usage 
        WHERE dateEpochDay < :olderThanEpochDay
    """)
    suspend fun pruneOldRecords(olderThanEpochDay: Long)
}
