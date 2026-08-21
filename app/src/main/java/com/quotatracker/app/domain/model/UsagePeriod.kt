package com.quotatracker.app.domain.model

import com.quotatracker.app.util.DateUtils
import java.time.ZoneId

enum class UsagePeriod(val label: String) {
    DAILY("Hari Ini"),
    WEEKLY("Minggu"),
    MONTHLY("Bulan");

    /**
     * Returns pair of (startTimeMillis, endTimeMillis).
     */
    fun getTimeRange(
        cycleDay: Int = 1,
        nowMillis: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Pair<Long, Long> {
        val start = when (this) {
            DAILY -> DateUtils.getStartOfToday(nowMillis, zone)
            WEEKLY -> DateUtils.getStartOfWeek(nowMillis, zone)
            MONTHLY -> DateUtils.getStartOfMonth(cycleDay, nowMillis, zone)
        }
        return Pair(start, nowMillis)
    }
}
