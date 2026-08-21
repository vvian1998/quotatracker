package com.quotatracker.app

import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

    private val jakarta = ZoneId.of("Asia/Jakarta")

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
        val now = Instant.parse("2026-08-21T12:00:00Z").toEpochMilli()
        val (dailyStart, dailyEnd) = UsagePeriod.DAILY.getTimeRange(
            nowMillis = now,
            zone = jakarta
        )
        assertEquals(now, dailyEnd)
        assertTrue(dailyStart <= dailyEnd)

        val (weeklyStart, weeklyEnd) = UsagePeriod.WEEKLY.getTimeRange(
            nowMillis = now,
            zone = jakarta
        )
        assertTrue(weeklyStart <= weeklyEnd)
        assertTrue(weeklyStart <= dailyStart)

        val (monthlyStart, monthlyEnd) = UsagePeriod.MONTHLY.getTimeRange(
            cycleDay = 15,
            nowMillis = now,
            zone = jakarta
        )
        assertTrue(monthlyStart <= monthlyEnd)
        assertTrue(monthlyStart < dailyStart)
    }

    @Test
    fun epochDayUsesLocalTimezone() {
        val now = Instant.parse("2026-08-21T00:30:00Z").toEpochMilli()
        val zone = ZoneId.of("America/Los_Angeles")
        val expectedDate = LocalDate.of(2026, 8, 20)
        val epochDay = DateUtils.todayEpochDay(now, zone)

        assertEquals(expectedDate.toEpochDay(), epochDay)
        assertEquals(
            expectedDate.atStartOfDay(zone).toInstant().toEpochMilli(),
            DateUtils.epochDayToStartMillis(epochDay, zone)
        )
    }

    @Test
    fun cycleDayStartsInPreviousMonthBeforeReset() {
        val now = LocalDate.of(2026, 8, 10)
            .atTime(12, 0)
            .atZone(jakarta)
            .toInstant()
            .toEpochMilli()

        val start = DateUtils.getStartOfMonth(15, now, jakarta)
        val expected = LocalDate.of(2026, 7, 15)
            .atStartOfDay(jakarta)
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, start)
    }
}
