package com.quotatracker.app.data.repository

import com.quotatracker.app.data.local.db.dao.QuotaSettingDao
import com.quotatracker.app.data.local.db.entity.QuotaSettingEntity
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.domain.model.QuotaSetting
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class QuotaRepository(
    private val quotaSettingDao: QuotaSettingDao,
    private val userPreferences: UserPreferences
) {

    /**
     * Get the global device quota setting (combines Room + UserPreferences)
     */
    fun getGlobalQuota(): Flow<QuotaSetting> = combine(
        quotaSettingDao.getGlobalQuotaFlow(),
        userPreferences.globalQuotaBytesFlow,
        userPreferences.warningPercentFlow
    ) { entity, prefBytes, prefWarning ->
        if (entity != null) {
            QuotaSetting(
                id = 0,
                quotaLimitBytes = entity.quotaLimitBytes,
                periodType = try { UsagePeriod.valueOf(entity.periodType) } catch (e: Exception) { UsagePeriod.MONTHLY },
                warningPercentage = entity.warningPercentage,
                isEnabled = entity.isEnabled
            )
        } else {
            QuotaSetting(
                id = 0,
                quotaLimitBytes = prefBytes,
                periodType = UsagePeriod.MONTHLY,
                warningPercentage = prefWarning,
                isEnabled = true
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Set the global device quota limit
     */
    suspend fun setGlobalQuota(quotaBytes: Long, warningPercent: Int = Constants.DEFAULT_WARNING_PERCENT) = withContext(Dispatchers.IO) {
        userPreferences.setGlobalQuotaBytes(quotaBytes)
        userPreferences.setWarningPercent(warningPercent)
        quotaSettingDao.insertOrUpdate(
            QuotaSettingEntity(
                id = 0,
                quotaLimitBytes = quotaBytes,
                periodType = "MONTHLY",
                warningPercentage = warningPercent,
                isEnabled = true
            )
        )
    }

    /**
     * Get per-app quota for a specific UID
     */
    fun getAppQuota(uid: Int): Flow<QuotaSetting?> = quotaSettingDao.getAppQuotaFlow(uid).combine(
        userPreferences.warningPercentFlow
    ) { entity, _ ->
        entity?.let {
            QuotaSetting(
                id = it.id,
                quotaLimitBytes = it.quotaLimitBytes,
                periodType = try { UsagePeriod.valueOf(it.periodType) } catch (e: Exception) { UsagePeriod.MONTHLY },
                warningPercentage = it.warningPercentage,
                isEnabled = it.isEnabled
            )
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Set per-app quota limit for a specific UID
     */
    suspend fun setAppQuota(uid: Int, limitBytes: Long, isEnabled: Boolean = true) = withContext(Dispatchers.IO) {
        if (limitBytes <= 0 || !isEnabled) {
            quotaSettingDao.deleteQuota(uid)
        } else {
            quotaSettingDao.insertOrUpdate(
                QuotaSettingEntity(
                    id = uid,
                    quotaLimitBytes = limitBytes,
                    periodType = "MONTHLY",
                    warningPercentage = 80,
                    isEnabled = isEnabled
                )
            )
        }
    }
}
