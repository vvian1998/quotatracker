package com.quotatracker.app.di

import android.content.Context
import androidx.room.Room
import com.quotatracker.app.data.local.db.AppDatabase
import com.quotatracker.app.data.local.db.dao.DataUsageDao
import com.quotatracker.app.data.local.db.dao.QuotaSettingDao
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.data.repository.DataUsageRepository
import com.quotatracker.app.data.repository.QuotaRepository
import com.quotatracker.app.data.system.AppUsageHelper
import com.quotatracker.app.data.system.NetworkStatsHelper
import com.quotatracker.app.data.system.TrafficStatsHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "quota_tracker_db"
        ).addMigrations(AppDatabase.MIGRATION_1_2).build()
    }

    @Provides
    fun provideDataUsageDao(database: AppDatabase): DataUsageDao {
        return database.dataUsageDao()
    }

    @Provides
    fun provideQuotaSettingDao(database: AppDatabase): QuotaSettingDao {
        return database.quotaSettingDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    @Provides
    @Singleton
    fun provideNetworkStatsHelper(@ApplicationContext context: Context): NetworkStatsHelper {
        return NetworkStatsHelper(context)
    }

    @Provides
    @Singleton
    fun provideAppUsageHelper(@ApplicationContext context: Context): AppUsageHelper {
        return AppUsageHelper(context)
    }

    @Provides
    @Singleton
    fun provideTrafficStatsHelper(): TrafficStatsHelper {
        return TrafficStatsHelper()
    }

    @Provides
    @Singleton
    fun provideDataUsageRepository(
        networkStatsHelper: NetworkStatsHelper,
        appUsageHelper: AppUsageHelper,
        dataUsageDao: DataUsageDao
    ): DataUsageRepository {
        return DataUsageRepository(networkStatsHelper, appUsageHelper, dataUsageDao)
    }

    @Provides
    @Singleton
    fun provideQuotaRepository(
        quotaSettingDao: QuotaSettingDao,
        userPreferences: UserPreferences
    ): QuotaRepository {
        return QuotaRepository(quotaSettingDao, userPreferences)
    }
}
