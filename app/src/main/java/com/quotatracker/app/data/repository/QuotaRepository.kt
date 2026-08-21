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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class QuotaRepository(
    private val quotaSettingDao: QuotaSettingDao,
    private val userPreferences: UserPreferences
) {
    /**
     * DataStore is the single source of truth for the global quota and warning
     * threshold. Room remains available for backwards-compatible persistence
     * and per-application informational limits.
     */
    fun getGlobalQuota(): Flow<QuotaSetting> = combine(
        userPreferences.globalQuotaBytesFlow,
        userPreferences.warningPercentFlow
    ) { prefBytes, prefWarning ->
        QuotaSetting(
            id = 0,
            quotaLimitBytes = prefBytes,
            periodType = UsagePeriod.MONTHLY,
            warningPercentage = prefWarning,
            isEnabled = true
        )
    }.flowOn(Dispatchers.IO)

    suspend fun setGlobalQuota(
        quotaBytes: Long,
        warningPercent: Int = Constants.DEFAULT_WARNING_PERCENT
    ) = withContext(Dispatchers.IO) {
        userPreferences.setGlobalQuotaBytes(quotaBytes)
        userPreferences.setWarningPercent(warningPercent)
        quotaSettingDao.insertOrUpdate(
            QuotaSettingEntity(
                id = 0,
                quotaLimitBytes = quotaBytes.coerceAtLeast(1L),
                periodType = UsagePeriod.MONTHLY.name,
                warningPercentage = warningPercent.coerceIn(50, 95),
                isEnabled = true
            )
        )
    }

    /**
     * Per-app quotas are informational references only. They do not block or
     * throttle network access because this app has no enforcement capability.
     */
    fun getAppQuota(uid: Int): Flow<QuotaSetting?> =
        quotaSettingDao.getAppQuotaFlow(uid).map { entity ->
            entity?.let {
                QuotaSetting(
                    id = it.id,
                    quotaLimitBytes = it.quotaLimitBytes,
                    periodType = try {
                        UsagePeriod.valueOf(it.periodType)
                    } catch (_: Exception) {
                        UsagePeriod.MONTHLY
                    },
                    warningPercentage = it.warningPercentage,
                    isEnabled = it.isEnabled
                )
            }
        }.flowOn(Dispatchers.IO)

    suspend fun setAppQuota(uid: Int, limitBytes: Long, isEnabled: Boolean = true) =
        withContext(Dispatchers.IO) {
            if (limitBytes <= 0 || !isEnabled) {
                quotaSettingDao.deleteQuota(uid)
            } else {
                quotaSettingDao.insertOrUpdate(
                    QuotaSettingEntity(
                        id = uid,
                        quotaLimitBytes = limitBytes,
                        periodType = UsagePeriod.MONTHLY.name,
                        warningPercentage = 80,
                        isEnabled = isEnabled
                    )
                )
            }
        }
}
