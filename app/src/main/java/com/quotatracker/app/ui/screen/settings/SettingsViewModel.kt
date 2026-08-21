package com.quotatracker.app.ui.screen.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.data.repository.QuotaRepository
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
import javax.inject.Inject

data class SettingsUiState(
    val monthlyQuotaGb: Float = 5.0f,
    val cycleDay: Int = 1,
    val isBubbleEnabled: Boolean = false,
    val isWarningEnabled: Boolean = true,
    val warningPercent: Int = 80,
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
                _uiState.value = _uiState.value.copy(monthlyQuotaGb = DataFormatter.bytesToGb(bytes).toFloat())
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
        _uiState.value = _uiState.value.copy(monthlyQuotaGb = gb)
        val bytes = DataFormatter.gbToBytes(gb.toDouble())
        viewModelScope.launch {
            quotaRepository.setGlobalQuota(bytes, _uiState.value.warningPercent)
        }
    }

    fun setCycleDay(day: Int) {
        _uiState.value = _uiState.value.copy(cycleDay = day)
        viewModelScope.launch {
            userPreferences.setQuotaCycleDay(day)
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
                context.startService(intent)
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
        _uiState.value = _uiState.value.copy(warningPercent = percent)
        viewModelScope.launch {
            userPreferences.setWarningPercent(percent)
        }
    }

    fun setAutoStartEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAutoStartEnabled = enabled)
        viewModelScope.launch {
            userPreferences.setAutoStartOnBoot(enabled)
        }
    }
}
