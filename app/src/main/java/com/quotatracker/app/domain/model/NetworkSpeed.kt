package com.quotatracker.app.domain.model

import com.quotatracker.app.util.DataFormatter

data class NetworkSpeed(
    val downloadBps: Long = 0L,
    val uploadBps: Long = 0L
) {
    val totalBps: Long get() = downloadBps + uploadBps

    fun formattedDownload(): String = DataFormatter.formatSpeed(downloadBps)
    fun formattedUpload(): String = DataFormatter.formatSpeed(uploadBps)
    fun formattedTotal(): String = DataFormatter.formatSpeed(totalBps)
}
