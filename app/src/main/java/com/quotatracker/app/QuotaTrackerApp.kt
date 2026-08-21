package com.quotatracker.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuotaTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
