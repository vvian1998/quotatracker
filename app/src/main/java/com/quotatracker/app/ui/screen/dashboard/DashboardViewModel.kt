package com.quotatracker.app.ui.screen.dashboard

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.data.repository.DataUsageRepository
import com.quotatracker.app.data.repository.QuotaRepository
import com.quotatracker.app.data.system.DeviceNetworkSummary
import com.quotatracker.app.domain.model.AppDataUsage
import com.quotatracker.app.domain.model.QuotaSetting
import com.quotatracker.app.domain.model.UsagePeriod
import com.quotatracker.app.service.FloatingBubbleService
import com.quotatracker.app.util.Constants
import com.quotatracker.app.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val selectedPeriod: UsagePeriod = UsagePeriod.DAILY,
    val appUsageList: List<AppDataUsage> = emptyList(),
    val deviceSummary: DeviceNetworkSummary = DeviceNetworkSummary(),
    val quotaSetting: QuotaSetting = QuotaSetting(),
    val isBubbleEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val maxUsageBytes: Long = 0L
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataUsageRepository: DataUsageRepository,
    private val quotaRepository: QuotaRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeBubbleState()
    }

    private fun observeBubbleState() {
        viewModelScope.launch {
            userPreferences.bubbleEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isBubbleEnabled = enabled)
            }
        }
    }

    fun setPeriod(period: UsagePeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. Quota
            try {
                val quota = quotaRepository.getGlobalQuota().first()
                _uiState.value = _uiState.value.copy(quotaSetting = quota)
            } catch (e: Exception) {
                // Keep existing quota if error
            }
        }

        viewModelScope.launch {
            val period = _uiState.value.selectedPeriod
            // 2. Device summary (Download / Upload)
            dataUsageRepository.getDeviceSummary(period).collect { summary ->
                _uiState.value = _uiState.value.copy(deviceSummary = summary)
            }
        }

        viewModelScope.launch {
            val period = _uiState.value.selectedPeriod
            // 3. Apps usage list
            dataUsageRepository.getAppUsageForPeriod(period).collect { list ->
                val max = list.firstOrNull()?.totalBytes ?: 0L
                _uiState.value = _uiState.value.copy(
                    appUsageList = list,
                    maxUsageBytes = max,
                    isLoading = false
                )
            }
        }

        // Trigger background snapshot to Room DB
        viewModelScope.launch {
            dataUsageRepository.syncTodayUsageToDb()
        }
    }

    fun toggleFloatingBubble(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !PermissionUtils.hasOverlayPermission(context)) {
                PermissionUtils.openOverlaySettings(context)
                return@launch
            }

            userPreferences.setBubbleEnabled(enabled)
            val intent = Intent(context, FloatingBubbleService::class.java).apply {
                action = if (enabled) Constants.ACTION_START_BUBBLE else Constants.ACTION_STOP_BUBBLE
            }
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }
    }
}
