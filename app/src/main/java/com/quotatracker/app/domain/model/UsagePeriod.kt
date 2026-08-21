package com.quotatracker.app.domain.model

import com.quotatracker.app.util.DateUtils

enum class UsagePeriod(val label: String) {
    DAILY("Hari Ini"),
    WEEKLY("Minggu"),
    MONTHLY("Bulan");

    /**
     * Returns pair of (startTimeMillis, endTimeMillis)
     */
    fun getTimeRange(cycleDay: Int = 1): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val start = when (this) {
            DAILY -> DateUtils.getStartOfToday()
            WEEKLY -> DateUtils.getStartOfWeek()
            MONTHLY -> DateUtils.getStartOfMonth(cycleDay)
        }
        return Pair(start, now)
    }
}
