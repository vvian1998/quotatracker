package com.quotatracker.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quotatracker.app.data.local.db.dao.DataUsageDao
import com.quotatracker.app.data.local.db.dao.QuotaSettingDao
import com.quotatracker.app.data.local.db.entity.AppDataUsageEntity
import com.quotatracker.app.data.local.db.entity.QuotaSettingEntity

@Database(
    entities = [
        AppDataUsageEntity::class,
        QuotaSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataUsageDao(): DataUsageDao
    abstract fun quotaSettingDao(): QuotaSettingDao

    companion object {
        /**
         * Version 2 keeps the existing schema intact. The explicit migration
         * prevents Room from deleting usage history/settings on upgrade.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema change is required for this version.
            }
        }
    }
}
