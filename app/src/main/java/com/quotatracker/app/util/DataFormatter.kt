package com.quotatracker.app.util

import java.util.Locale

object DataFormatter {

    /**
     * Format raw byte count into human-readable string (e.g., "850 MB", "2.35 GB", "45.2 KB")
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        val tb = gb / 1024.0

        return when {
            tb >= 1.0 -> String.format(Locale.US, "%.2f TB", tb)
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    /**
     * Format bytes with separate number and unit for custom styling
     */
    fun formatBytesParts(bytes: Long): Pair<String, String> {
        if (bytes <= 0) return Pair("0", "B")
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        val tb = gb / 1024.0

        return when {
            tb >= 1.0 -> Pair(String.format(Locale.US, "%.2f", tb), "TB")
            gb >= 1.0 -> Pair(String.format(Locale.US, "%.1f", gb), "GB")
            mb >= 1.0 -> Pair(String.format(Locale.US, "%.1f", mb), "MB")
            kb >= 1.0 -> Pair(String.format(Locale.US, "%.1f", kb), "KB")
            else -> Pair(bytes.toString(), "B")
        }
    }

    /**
     * Format speed in Bytes/s to readable string (e.g. "2.5 MB/s")
     */
    fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 B/s"
        val kb = bytesPerSec / 1024.0
        val mb = kb / 1024.0

        return when {
            mb >= 1.0 -> String.format(Locale.US, "%.2f MB/s", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB/s", kb)
            else -> "$bytesPerSec B/s"
        }
    }

    /**
     * Convert GB value to Bytes
     */
    fun gbToBytes(gb: Double): Long {
        return (gb * 1024 * 1024 * 1024).toLong()
    }

    /**
     * Convert Bytes to GB value
     */
    fun bytesToGb(bytes: Long): Double {
        return bytes / (1024.0 * 1024.0 * 1024.0)
    }

    /**
     * Calculate percentage safely (0.0 to 1.0)
     */
    fun calculatePercentage(used: Long, total: Long): Float {
        if (total <= 0) return 0f
        return (used.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}
