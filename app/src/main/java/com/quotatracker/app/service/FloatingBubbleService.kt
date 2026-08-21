package com.quotatracker.app.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.TrafficStats
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.quotatracker.app.MainActivity
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.data.repository.DataUsageRepository
import com.quotatracker.app.data.system.AppUsageHelper
import com.quotatracker.app.util.Constants
import com.quotatracker.app.util.DataFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class FloatingBubbleService : Service() {

    private val tag = "FloatingBubbleService"
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    @Inject
    lateinit var appUsageHelper: AppUsageHelper

    @Inject
    lateinit var dataUsageRepository: DataUsageRepository

    @Inject
    lateinit var userPreferences: UserPreferences

    private var windowManager: WindowManager? = null
    private var bubbleContainer: LinearLayout? = null
    private var textAppName: TextView? = null
    private var textUsage: TextView? = null
    private var dotLive: View? = null
    private var iconApp: ImageView? = null

    private var windowParams: WindowManager.LayoutParams? = null
    private var currentTrackedPackage: String? = null
    private var currentTrackedUid: Int = -1
    private var baselineTodayUsage: Long = 0L
    private var baselineTrafficBytes: Long = 0L
    private var prevTrafficBytes: Long = 0L
    private var prevTimestamp: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startAsForeground()

        if (!Settings.canDrawOverlays(this)) {
            Log.w(tag, "Overlay permission not granted. Stopping service.")
            stopSelf()
            return
        }

        setupBubbleView()
        startTrackingLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_STOP_BUBBLE) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.CHANNEL_BUBBLE_ID,
            Constants.CHANNEL_BUBBLE_NAME,
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = "Menampilkan balon pemantau kuota di atas layar"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun startAsForeground() {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, Constants.CHANNEL_BUBBLE_ID)
            .setContentTitle("Floating Bubble Aktif")
            .setContentText("Melacak kuota aplikasi yang sedang dibuka")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Constants.NOTIFICATION_BUBBLE_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(Constants.NOTIFICATION_BUBBLE_ID, notification)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBubbleView() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 180
        }

        val dp12 = dpToPx(12f)
        val dp8 = dpToPx(8f)
        val dp14 = dpToPx(14f)

        // Frosted Glass Bubble Background (#1A2633 with teal border)
        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp14.toFloat()
            setColor(0xD91A2633.toInt()) // 85% alpha dark navy
            setStroke(dpToPx(1.2f), 0x8000BFA5.toInt()) // Teal border
        }

        bubbleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = bgDrawable
            setPadding(dp14, dp8, dp14, dp8)
            elevation = dpToPx(8f).toFloat()

            // Header row: App Name + Live indicator dot
            val headerRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            textAppName = TextView(context).apply {
                text = "QuotaTracker"
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            dotLive = View(context).apply {
                val dotDrawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0xFF00BFA5.toInt())
                }
                background = dotDrawable
                layoutParams = LinearLayout.LayoutParams(dpToPx(6f), dpToPx(6f)).apply {
                    marginStart = dpToPx(6f)
                }
            }

            headerRow.addView(textAppName)
            headerRow.addView(dotLive)

            // Usage row: Download arrow + Accumulated Bytes
            textUsage = TextView(context).apply {
                text = "↓ 0 B hari ini"
                setTextColor(0xFF00BFA5.toInt())
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, dpToPx(2f), 0, 0)
            }

            addView(headerRow)
            addView(textUsage)
        }

        // Drag and Click Listener
        bubbleContainer?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val params = windowParams ?: return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - initialTouchX
                        val dy = event.rawY - initialTouchY

                        if (abs(dx) > 10 || abs(dy) > 10) {
                            isDragging = true
                        }

                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        windowManager?.updateViewLayout(bubbleContainer, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            // Single tap: open main app
                            val openIntent = Intent(this@FloatingBubbleService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                            startActivity(openIntent)
                        }
                        return true
                    }
                }
                return false
            }
        })

        try {
            windowManager?.addView(bubbleContainer, windowParams)
        } catch (e: Exception) {
            Log.e(tag, "Failed to add bubble overlay: ${e.message}", e)
        }
    }

    private fun startTrackingLoop() {
        serviceScope.launch {
            while (isActive) {
                try {
                    val foregroundPackage = withContext(Dispatchers.IO) {
                        appUsageHelper.getCurrentForegroundPackage()
                    }

                    if (!foregroundPackage.isNullOrEmpty() && foregroundPackage != packageName) {
                        if (foregroundPackage != currentTrackedPackage) {
                            currentTrackedPackage = foregroundPackage
                            val appInfo = withContext(Dispatchers.IO) {
                                appUsageHelper.resolveAppInfoByPackage(foregroundPackage)
                            }

                            if (appInfo != null) {
                                currentTrackedUid = appInfo.uid
                                textAppName?.text = appInfo.appName

                                // Initialize baseline live traffic from kernel sockets
                                val curRx = TrafficStats.getUidRxBytes(currentTrackedUid)
                                val curTx = TrafficStats.getUidTxBytes(currentTrackedUid)
                                val initialTraffic = if (curRx >= 0) curRx + curTx.coerceAtLeast(0L) else 0L

                                val baseUsage = withContext(Dispatchers.IO) {
                                    dataUsageRepository.getTodayUsageForUid(currentTrackedUid)
                                }

                                baselineTodayUsage = baseUsage
                                baselineTrafficBytes = initialTraffic
                                prevTrafficBytes = initialTraffic
                                prevTimestamp = System.currentTimeMillis()

                                textUsage?.text = "↓ ${DataFormatter.formatBytes(baseUsage)}"
                            }
                        } else if (currentTrackedUid > 0) {
                            // Same app is active - compute realtime live bytes & speed
                            updateLiveUsageAndSpeed()
                        }
                    } else if (foregroundPackage == packageName) {
                        textAppName?.text = "QuotaTracker"
                        textUsage?.text = "Aktif memantau"
                        currentTrackedPackage = foregroundPackage
                        currentTrackedUid = -1
                    } else {
                        // When foregroundPackage is temporarily null/transient, keep updating current tracked app if active
                        if (currentTrackedUid > 0) {
                            updateLiveUsageAndSpeed()
                        }
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Error in bubble tracking loop: ${e.message}")
                }

                delay(Constants.FOREGROUND_POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun updateLiveUsageAndSpeed() {
        val curRx = TrafficStats.getUidRxBytes(currentTrackedUid)
        val curTx = TrafficStats.getUidTxBytes(currentTrackedUid)
        val now = System.currentTimeMillis()

        if (curRx >= 0) {
            val curTotal = curRx + curTx.coerceAtLeast(0L)
            val delta = (curTotal - baselineTrafficBytes).coerceAtLeast(0L)
            val currentDisplayUsage = baselineTodayUsage + delta

            val timeDeltaSec = (now - prevTimestamp) / 1000.0
            val speedBps = if (timeDeltaSec > 0 && prevTrafficBytes > 0 && curTotal >= prevTrafficBytes) {
                ((curTotal - prevTrafficBytes) / timeDeltaSec).toLong()
            } else 0L

            prevTrafficBytes = curTotal
            prevTimestamp = now

            if (speedBps > 1024L) {
                textUsage?.text = "↓ ${DataFormatter.formatSpeed(speedBps)} • ${DataFormatter.formatBytes(currentDisplayUsage)}"
            } else {
                textUsage?.text = "↓ ${DataFormatter.formatBytes(currentDisplayUsage)}"
            }
        } else {
            // Fallback for devices where getUidRxBytes returns UNSUPPORTED
            val todayUsage = withContext(Dispatchers.IO) {
                dataUsageRepository.getTodayUsageForUid(currentTrackedUid)
            }
            textUsage?.text = "↓ ${DataFormatter.formatBytes(todayUsage)}"
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                userPreferences.setBubbleEnabled(false)
            } catch (e: Exception) {
                Log.w(tag, "Error resetting bubble preference: ${e.message}")
            }
        }
        serviceScope.cancel()
        bubbleContainer?.let { view ->
            try {
                if (view.isAttachedToWindow) {
                    windowManager?.removeView(view)
                }
            } catch (e: Exception) {
                Log.w(tag, "Error removing bubble view: ${e.message}")
            } finally {
                bubbleContainer = null
            }
        }
    }
}
