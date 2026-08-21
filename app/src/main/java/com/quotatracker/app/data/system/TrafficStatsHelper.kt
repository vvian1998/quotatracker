package com.quotatracker.app.data.system

import android.net.TrafficStats
import com.quotatracker.app.domain.model.NetworkSpeed
import com.quotatracker.app.util.Constants
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class TrafficStatsHelper {

    /**
     * Emits device-wide real-time network speed at given interval
     */
    fun observeNetworkSpeed(intervalMs: Long = Constants.SPEED_MONITOR_INTERVAL_MS): Flow<NetworkSpeed> = flow {
        var prevRx = TrafficStats.getTotalRxBytes()
        var prevTx = TrafficStats.getTotalTxBytes()
        var prevTime = System.currentTimeMillis()

        while (currentCoroutineContext().isActive) {
            delay(intervalMs)
            val currentRx = TrafficStats.getTotalRxBytes()
            val currentTx = TrafficStats.getTotalTxBytes()
            val currentTime = System.currentTimeMillis()

            val timeDeltaSec = (currentTime - prevTime) / 1000.0
            if (timeDeltaSec > 0 && prevRx != TrafficStats.UNSUPPORTED.toLong()) {
                val rxSpeed = ((currentRx - prevRx) / timeDeltaSec).toLong().coerceAtLeast(0L)
                val txSpeed = ((currentTx - prevTx) / timeDeltaSec).toLong().coerceAtLeast(0L)
                emit(NetworkSpeed(downloadBps = rxSpeed, uploadBps = txSpeed))
            }

            prevRx = currentRx
            prevTx = currentTx
            prevTime = currentTime
        }
    }
}
