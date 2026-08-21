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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val selectedPeriod: UsagePeriod = UsagePeriod.DAILY,
    val cycleDay: Int = Constants.DEFAULT_CYCLE_DAY,
    val appUsageList: List<AppDataUsage> = emptyList(),
    val deviceSummary: DeviceNetworkSummary = DeviceNetworkSummary(),
    val quotaSetting: QuotaSetting = QuotaSetting(),
    val isBubbleEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val maxUsageBytes: Long = 0L,
    val errorMessage: String? = null
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
    private var loadJob: Job? = null
    private var cycleDayInitialized = false

    init {
        observeBubbleState()
        observeCycleDay()
        loadData()
    }

    private fun observeBubbleState() {
        viewModelScope.launch {
            userPreferences.bubbleEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isBubbleEnabled = enabled)
            }
        }
    }

    private fun observeCycleDay() {
        viewModelScope.launch {
            userPreferences.quotaCycleDayFlow.collect { day ->
                _uiState.value = _uiState.value.copy(cycleDay = day)
                if (cycleDayInitialized) loadData()
                cycleDayInitialized = true
            }
        }
    }

    fun setPeriod(period: UsagePeriod) {
        if (_uiState.value.selectedPeriod == period) return
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadData()
    }

    fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            if (!PermissionUtils.hasUsageStatsPermission(context)) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Izin Usage Access belum aktif. Aktifkan izin tersebut untuk membaca data penggunaan."
                )
                return@launch
            }

            try {
                val period = _uiState.value.selectedPeriod
                val cycleDay = userPreferences.quotaCycleDayFlow.first()
                val quota = quotaRepository.getGlobalQuota().first()
                val summary = dataUsageRepository
                    .getDeviceSummary(period, cycleDay)
                    .first()
                val appUsage = dataUsageRepository
                    .getAppUsageForPeriod(period, cycleDay)
                    .first()
                val max = appUsage.firstOrNull()?.totalBytes ?: 0L

                _uiState.value = _uiState.value.copy(
                    cycleDay = cycleDay,
                    quotaSetting = quota,
                    deviceSummary = summary,
                    appUsageList = appUsage,
                    maxUsageBytes = max,
                    isLoading = false,
                    errorMessage = null
                )
                dataUsageRepository.syncTodayUsageToDb()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Gagal membaca NetworkStats. Periksa Usage Access lalu coba lagi."
                )
            }
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
