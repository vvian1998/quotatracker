package com.quotatracker.app.ui.screen.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.data.repository.QuotaRepository
import com.quotatracker.app.service.DataUsageSyncWorker
import com.quotatracker.app.service.FloatingBubbleService
import com.quotatracker.app.util.Constants
import com.quotatracker.app.util.DataFormatter
import com.quotatracker.app.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val monthlyQuotaGb: Float = 5.0f,
    val monthlyQuotaInputGb: String = "5.0",
    val cycleDay: Int = Constants.DEFAULT_CYCLE_DAY,
    val isBubbleEnabled: Boolean = false,
    val isWarningEnabled: Boolean = true,
    val warningPercent: Int = Constants.DEFAULT_WARNING_PERCENT,
    val isAutoStartEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences,
    private val quotaRepository: QuotaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            userPreferences.globalQuotaBytesFlow.collect { bytes ->
                val gb = DataFormatter.bytesToGb(bytes).toFloat()
                _uiState.value = _uiState.value.copy(
                    monthlyQuotaGb = gb,
                    monthlyQuotaInputGb = formatGb(gb)
                )
            }
        }

        viewModelScope.launch {
            userPreferences.quotaCycleDayFlow.collect { day ->
                _uiState.value = _uiState.value.copy(cycleDay = day)
            }
        }

        viewModelScope.launch {
            userPreferences.bubbleEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isBubbleEnabled = enabled)
            }
        }

        viewModelScope.launch {
            userPreferences.warningEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isWarningEnabled = enabled)
            }
        }

        viewModelScope.launch {
            userPreferences.warningPercentFlow.collect { percent ->
                _uiState.value = _uiState.value.copy(warningPercent = percent)
            }
        }

        viewModelScope.launch {
            userPreferences.autoStartOnBootFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(isAutoStartEnabled = enabled)
            }
        }
    }

    fun setMonthlyQuotaGb(gb: Float) {
        val safeGb = gb.coerceIn(1.0f, 100.0f)
        _uiState.value = _uiState.value.copy(
            monthlyQuotaGb = safeGb,
            monthlyQuotaInputGb = formatGb(safeGb)
        )
        saveGlobalQuota(safeGb)
    }

    fun setMonthlyQuotaInput(value: String) {
        val normalized = value.replace(',', '.')
        if (!normalized.matches(Regex("\\d{0,3}(\\.\\d{0,2})?"))) return

        _uiState.value = _uiState.value.copy(monthlyQuotaInputGb = normalized)
        normalized.toFloatOrNull()
            ?.takeIf { it in 1.0f..100.0f }
            ?.let { safeGb ->
                _uiState.value = _uiState.value.copy(monthlyQuotaGb = safeGb)
                saveGlobalQuota(safeGb)
            }
    }

    private fun saveGlobalQuota(gb: Float) {
        val bytes = DataFormatter.gbToBytes(gb.toDouble())
        viewModelScope.launch {
            quotaRepository.setGlobalQuota(bytes, _uiState.value.warningPercent)
        }
    }

    fun setCycleDay(day: Int) {
        val safeDay = day.coerceIn(1, 28)
        _uiState.value = _uiState.value.copy(cycleDay = safeDay)
        viewModelScope.launch {
            userPreferences.setQuotaCycleDay(safeDay)
        }
    }

    fun setBubbleEnabled(enabled: Boolean) {
        if (enabled && !PermissionUtils.hasOverlayPermission(context)) {
            PermissionUtils.openOverlaySettings(context)
            return
        }

        _uiState.value = _uiState.value.copy(isBubbleEnabled = enabled)
        viewModelScope.launch {
            userPreferences.setBubbleEnabled(enabled)
            val intent = Intent(context, FloatingBubbleService::class.java).apply {
                action = if (enabled) Constants.ACTION_START_BUBBLE else Constants.ACTION_STOP_BUBBLE
            }
            if (enabled) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        }
    }

    fun setWarningEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isWarningEnabled = enabled)
        viewModelScope.launch {
            userPreferences.setWarningEnabled(enabled)
        }
    }

    fun setWarningPercent(percent: Int) {
        val safePercent = percent.coerceIn(50, 95)
        _uiState.value = _uiState.value.copy(warningPercent = safePercent)
        viewModelScope.launch {
            userPreferences.setWarningPercent(safePercent)
        }
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAutoStartEnabled = enabled)
        viewModelScope.launch {
            userPreferences.setAutoStartOnBoot(enabled)
            if (enabled) {
                DataUsageSyncWorker.schedule(context)
            } else {
                DataUsageSyncWorker.cancel(context)
            }
        }
    }

    private fun formatGb(gb: Float): String =
        String.format(Locale.US, "%.2f", gb).trimEnd('0').trimEnd('.')
}
