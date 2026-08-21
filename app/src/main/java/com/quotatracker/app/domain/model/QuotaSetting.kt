package com.quotatracker.app.domain.model

import com.quotatracker.app.util.Constants
import com.quotatracker.app.util.DataFormatter

data class QuotaSetting(
    val id: Int = 0, // 0 = global device quota, >0 = app uid quota
    val quotaLimitBytes: Long = Constants.DEFAULT_GLOBAL_QUOTA_BYTES,
    val periodType: UsagePeriod = UsagePeriod.MONTHLY,
    val warningPercentage: Int = Constants.DEFAULT_WARNING_PERCENT,
    val isEnabled: Boolean = true
) {
    val formattedLimit: String get() = DataFormatter.formatBytes(quotaLimitBytes)
    val limitInGb: Double get() = DataFormatter.bytesToGb(quotaLimitBytes)
}
