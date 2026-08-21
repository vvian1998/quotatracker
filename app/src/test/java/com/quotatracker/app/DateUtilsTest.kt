package com.quotatracker.app

import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.util.DateUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    @Test
    fun testTodayEpochDay() {
        val epochDay = DateUtils.todayEpochDay()
        assertTrue(epochDay > 0)
    }

    @Test
    fun testStartOfToday() {
        val startOfToday = DateUtils.getStartOfToday()
        val now = System.currentTimeMillis()
        assertTrue(startOfToday <= now)
        assertTrue(now - startOfToday < DateUtils.MILLIS_PER_DAY)
    }

    @Test
    fun testUsagePeriodTimeRanges() {
        val (dailyStart, dailyEnd) = UsagePeriod.DAILY.getTimeRange()
        assertTrue(dailyStart <= dailyEnd)

        val (weeklyStart, weeklyEnd) = UsagePeriod.WEEKLY.getTimeRange()
        assertTrue(weeklyStart <= weeklyEnd)
        assertTrue(weeklyStart <= dailyStart)

        val (monthlyStart, monthlyEnd) = UsagePeriod.MONTHLY.getTimeRange(1)
        assertTrue(monthlyStart <= monthlyEnd)
        assertTrue(monthlyStart <= dailyStart)
    }
}
