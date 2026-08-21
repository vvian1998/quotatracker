package com.quotatracker.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quotatracker.app.data.local.db.dao.DataUsageDao
import com.quotatracker.app.data.local.db.dao.QuotaSettingDao
import com.quotatracker.app.data.local.db.entity.AppDataUsageEntity
import com.quotatracker.app.data.local.db.entity.QuotaSettingEntity

@Database(
    entities = [
        AppDataUsageEntity::class,
        QuotaSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataUsageDao(): DataUsageDao
    abstract fun quotaSettingDao(): QuotaSettingDao
}
