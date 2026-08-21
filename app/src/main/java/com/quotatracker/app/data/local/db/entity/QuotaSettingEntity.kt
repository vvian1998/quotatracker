package com.quotatracker.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quota_settings")
data class QuotaSettingEntity(
    @PrimaryKey
    val id: Int = 0, // 0 = global device quota, >0 = app uid quota
    val quotaLimitBytes: Long,
    val periodType: String = "MONTHLY",
    val warningPercentage: Int = 80,
    val isEnabled: Boolean = true
)
