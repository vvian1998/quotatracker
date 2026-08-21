package com.quotatracker.app.util

import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Date
import java.util.Locale

object DateUtils {

    const val MILLIS_PER_DAY = 86_400_000L

    private fun localDate(nowMillis: Long, zone: ZoneId): LocalDate =
        Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()

    private fun LocalDate.startOfDayMillis(zone: ZoneId): Long =
        atStartOfDay(zone).toInstant().toEpochMilli()

    /**
     * Current local calendar day represented as an epoch day.
     * Epoch days remain stable Room keys, while conversion uses the device
     * timezone so records do not cross a local midnight at the UTC boundary.
     */
    fun todayEpochDay(
        nowMillis: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long = localDate(nowMillis, zone).toEpochDay()

    /**
     * Start of day timestamp (00:00:00.000) in the requested timezone.
     */
    fun getStartOfToday(
        nowMillis: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long = localDate(nowMillis, zone).startOfDayMillis(zone)

    /**
     * Start of current week timestamp (Monday 00:00:00.000).
     */
    fun getStartOfWeek(
        nowMillis: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long = localDate(nowMillis, zone)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .startOfDayMillis(zone)

    /**
     * Start of the current billing cycle. The supported cycle-day range is
     * 1..28 so every cycle date exists in every month.
     */
    fun getStartOfMonth(
        cycleDay: Int = 1,
        nowMillis: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): Long {
        val safeCycleDay = cycleDay.coerceIn(1, 28)
        val today = localDate(nowMillis, zone)
        val cycleMonth = if (today.dayOfMonth < safeCycleDay) today.minusMonths(1) else today
        val cycleDate = cycleMonth.withDayOfMonth(
            safeCycleDay.coerceAtMost(cycleMonth.lengthOfMonth())
        )
        return cycleDate.startOfDayMillis(zone)
    }

    /**
     * Formatted string for dates (e.g., "Kamis, 20 Agt").
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    /**
     * Day of week short label (e.g., "Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").
     */
    fun formatDayOfWeek(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    /**
     * Start timestamp for an epoch day in the requested timezone.
     */
    fun epochDayToStartMillis(
        epochDay: Long,
        zone: ZoneId = ZoneId.systemDefault()
    ): Long = LocalDate.ofEpochDay(epochDay).startOfDayMillis(zone)

    fun epochDayToEndMillis(
        epochDay: Long,
        zone: ZoneId = ZoneId.systemDefault()
    ): Long = epochDayToStartMillis(epochDay + 1, zone) - 1L
}
