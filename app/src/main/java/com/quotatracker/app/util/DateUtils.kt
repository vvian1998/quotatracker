package com.quotatracker.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    const val MILLIS_PER_DAY = 86_400_000L

    /**
     * Current day represented as epoch day (days since Jan 1, 1970 UTC)
     */
    fun todayEpochDay(): Long {
        return System.currentTimeMillis() / MILLIS_PER_DAY
    }

    /**
     * Start of day timestamp (00:00:00.000) for a given calendar instance
     */
    fun getStartOfToday(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Start of current week timestamp (Monday 00:00:00.000)
     */
    fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Start of current month timestamp (1st of month 00:00:00.000)
     * Or based on custom cycle day
     */
    fun getStartOfMonth(cycleDay: Int = 1): Long {
        val cal = Calendar.getInstance().apply {
            val currentDay = get(Calendar.DAY_OF_MONTH)
            if (currentDay < cycleDay) {
                // Billing cycle started last month
                add(Calendar.MONTH, -1)
            }
            val maxDayInMonth = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, cycleDay.coerceAtMost(maxDayInMonth))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /**
     * Formatted string for dates (e.g., "Kamis, 20 Agt")
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    /**
     * Day of week short label (e.g., "Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
     */
    fun formatDayOfWeek(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    /**
     * Start timestamp for an epoch day
     */
    fun epochDayToStartMillis(epochDay: Long): Long {
        return epochDay * MILLIS_PER_DAY
    }
}
