package com.quotatracker.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_data_usage",
    indices = [
        Index(value = ["uid", "dateEpochDay"], unique = true),
        Index(value = ["dateEpochDay"]),
        Index(value = ["packageName"])
    ]
)
data class AppDataUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uid: Int,
    val packageName: String,
    val appName: String,
    val dateEpochDay: Long, // Epoch day for grouping
    val mobileRxBytes: Long = 0L,
    val mobileTxBytes: Long = 0L,
    val wifiRxBytes: Long = 0L,
    val wifiTxBytes: Long = 0L,
    val foregroundBytes: Long = 0L,
    val backgroundBytes: Long = 0L,
    val recordedAt: Long = System.currentTimeMillis()
) {
    val totalMobileBytes: Long get() = mobileRxBytes + mobileTxBytes
    val totalWifiBytes: Long get() = wifiRxBytes + wifiTxBytes
    val grandTotalBytes: Long get() = totalMobileBytes + totalWifiBytes
}
