package com.quotatracker.app.data.system

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log

data class ResolvedAppInfo(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false
)

class AppUsageHelper(private val context: Context) {

    private val tag = "AppUsageHelper"
    private val packageManager: PackageManager = context.packageManager
    private val usageStatsManager: UsageStatsManager? by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    }

    private val appInfoCache = mutableMapOf<Int, ResolvedAppInfo>()

    /**
     * Detect the package name of the app currently in the foreground
     */
    fun getCurrentForegroundPackage(): String? {
        val usm = usageStatsManager ?: return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 60_000L // 60s rolling window

        return try {
            val events = usm.queryEvents(startTime, endTime)
            var lastForegroundPkg: String? = null
            var lastEventTime = 0L
            val event = UsageEvents.Event()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    if (event.timeStamp >= lastEventTime) {
                        lastForegroundPkg = event.packageName
                        lastEventTime = event.timeStamp
                    }
                }
            }

            // Fallback if no event in the last 60s (e.g. user watching long video in YouTube)
            if (lastForegroundPkg == null) {
                val stats = usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    endTime - (1000 * 60 * 120), // 2 hours
                    endTime
                )
                lastForegroundPkg = stats
                    ?.filter { it.packageName != "android" && it.packageName != "com.android.systemui" }
                    ?.maxByOrNull { it.lastTimeUsed }
                    ?.packageName
            }

            lastForegroundPkg
        } catch (e: Exception) {
            Log.w(tag, "Failed to query foreground events: ${e.message}")
            null
        }
    }

    /**
     * Resolve app information (label, icon) from UID with caching
     */
    fun resolveAppInfo(uid: Int): ResolvedAppInfo {
        appInfoCache[uid]?.let { return it }

        val packages = try {
            packageManager.getPackagesForUid(uid)
        } catch (e: Exception) {
            null
        }

        val packageName = packages?.firstOrNull() ?: "uid_$uid"

        // Handle special Android UIDs
        when (uid) {
            -4 -> {
                val info = ResolvedAppInfo(uid, "android.uid.removed", "Aplikasi Terhapus", null, true)
                appInfoCache[uid] = info
                return info
            }
            -5 -> {
                val info = ResolvedAppInfo(uid, "android.uid.tethering", "Tethering / Hotspot", null, true)
                appInfoCache[uid] = info
                return info
            }
            1000 -> {
                val info = ResolvedAppInfo(uid, "android.uid.system", "Sistem Android", null, true)
                appInfoCache[uid] = info
                return info
            }
        }

        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }

            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val icon = packageManager.getApplicationIcon(appInfo)
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            val resolved = ResolvedAppInfo(uid, packageName, appName, icon, isSystem)
            appInfoCache[uid] = resolved
            resolved
        } catch (e: Exception) {
            val fallback = ResolvedAppInfo(
                uid = uid,
                packageName = packageName,
                appName = if (packageName.startsWith("uid_")) "UID $uid" else packageName.substringAfterLast('.').capitalize(),
                icon = null,
                isSystemApp = false
            )
            appInfoCache[uid] = fallback
            fallback
        }
    }

    /**
     * Resolve app info directly from package name
     */
    fun resolveAppInfoByPackage(packageName: String): ResolvedAppInfo? {
        return try {
            val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val icon = packageManager.getApplicationIcon(appInfo)
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            ResolvedAppInfo(appInfo.uid, packageName, appName, icon, isSystem)
        } catch (e: Exception) {
            null
        }
    }

    private fun String.capitalize(): String {
        return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
