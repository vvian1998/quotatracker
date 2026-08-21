package com.quotatracker.app

import com.quotatracker.app.util.DataFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class DataFormatterTest {

    @Test
    fun testFormatBytesZeroAndNegative() {
        assertEquals("0 B", DataFormatter.formatBytes(0))
        assertEquals("0 B", DataFormatter.formatBytes(-500))
    }

    @Test
    fun testFormatBytesKb() {
        assertEquals("1.0 KB", DataFormatter.formatBytes(1024))
        assertEquals("500.0 KB", DataFormatter.formatBytes(500 * 1024))
    }

    @Test
    fun testFormatBytesMb() {
        assertEquals("1.0 MB", DataFormatter.formatBytes(1024 * 1024))
        assertEquals("850.0 MB", DataFormatter.formatBytes(850L * 1024 * 1024))
    }

    @Test
    fun testFormatBytesGb() {
        assertEquals("1.00 GB", DataFormatter.formatBytes(1024L * 1024 * 1024))
        assertEquals("2.35 GB", DataFormatter.formatBytes((2.35 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testFormatSpeed() {
        assertEquals("0 B/s", DataFormatter.formatSpeed(0))
        assertEquals("500.0 KB/s", DataFormatter.formatSpeed(500 * 1024))
        assertEquals("2.50 MB/s", DataFormatter.formatSpeed((2.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun testCalculatePercentage() {
        assertEquals(0.5f, DataFormatter.calculatePercentage(50, 100), 0.001f)
        assertEquals(1.0f, DataFormatter.calculatePercentage(150, 100), 0.001f)
        assertEquals(0.0f, DataFormatter.calculatePercentage(0, 100), 0.001f)
        assertEquals(0.0f, DataFormatter.calculatePercentage(50, 0), 0.001f)
    }
}
