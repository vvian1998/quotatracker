package com.quotatracker.app.domain.model

import android.graphics.drawable.Drawable

data class AppDataUsage(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val appIcon: Drawable? = null,
    val mobileBytes: Long = 0L,
    val wifiBytes: Long = 0L,
    val foregroundBytes: Long = 0L,
    val backgroundBytes: Long = 0L,
    val totalBytes: Long = mobileBytes + wifiBytes,
    val percentageOfTotal: Float = 0f
)
