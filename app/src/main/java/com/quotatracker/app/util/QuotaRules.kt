package com.quotatracker.app.util

object QuotaRules {
    fun shouldWarn(
        usedBytes: Long,
        quotaBytes: Long,
        warningPercent: Int,
        warningEnabled: Boolean
    ): Boolean {
        if (!warningEnabled || quotaBytes <= 0L) return false
        return usedBytes.toDouble() / quotaBytes.toDouble() >=
            warningPercent.coerceIn(1, 100) / 100.0
    }
}
