package com.quotatracker.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quotatracker.app.data.local.db.entity.QuotaSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotaSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(quotaSetting: QuotaSettingEntity)

    @Query("SELECT * FROM quota_settings WHERE id = 0")
    fun getGlobalQuotaFlow(): Flow<QuotaSettingEntity?>

    @Query("SELECT * FROM quota_settings WHERE id = 0")
    suspend fun getGlobalQuota(): QuotaSettingEntity?

    @Query("SELECT * FROM quota_settings WHERE id = :uid")
    fun getAppQuotaFlow(uid: Int): Flow<QuotaSettingEntity?>

    @Query("SELECT * FROM quota_settings WHERE id = :uid")
    suspend fun getAppQuota(uid: Int): QuotaSettingEntity?

    @Query("SELECT * FROM quota_settings WHERE id > 0")
    fun getAllAppQuotasFlow(): Flow<List<QuotaSettingEntity>>

    @Query("DELETE FROM quota_settings WHERE id = :uid")
    suspend fun deleteQuota(uid: Int)
}
