package com.quotatracker.app.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotatracker.app.data.local.preferences.UserPreferences
import com.quotatracker.app.data.repository.DataUsageRepository
import com.quotatracker.app.data.repository.QuotaRepository
import com.quotatracker.app.domain.model.AppDataUsage
import com.quotatracker.app.domain.model.QuotaSetting
import com.quotatracker.app.domain.model.UsagePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppDetailUiState(
    val uid: Int = 0,
    val appUsage: AppDataUsage? = null,
    val weeklyBreakdown: List<Pair<Long, Long>> = emptyList(), // Pair(timestamp, bytes)
    val appQuota: QuotaSetting? = null,
    val isLoading: Boolean = true,
    val isQuotaLimitEnabled: Boolean = false,
    val quotaLimitGb: Float = 1.0f
)

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataUsageRepository: DataUsageRepository,
    private val quotaRepository: QuotaRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val uid: Int = checkNotNull(savedStateHandle["uid"])
    private val _uiState = MutableStateFlow(AppDetailUiState(uid = uid))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    init {
        loadAppDetail()
    }

    fun loadAppDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 1. App Detail (Current billing cycle)
            val cycleDay = userPreferences.quotaCycleDayFlow.first()
            dataUsageRepository.getAppDetail(uid, UsagePeriod.MONTHLY, cycleDay).collect { usage ->
                _uiState.value = _uiState.value.copy(
                    appUsage = usage,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            // 2. Weekly 7-day breakdown
            dataUsageRepository.getAppWeeklyBreakdown(uid).collect { breakdown ->
                _uiState.value = _uiState.value.copy(weeklyBreakdown = breakdown)
            }
        }

        viewModelScope.launch {
            // 3. Per-App Quota setting
            quotaRepository.getAppQuota(uid).collect { quota ->
                _uiState.value = _uiState.value.copy(
                    appQuota = quota,
                    isQuotaLimitEnabled = quota?.isEnabled == true,
                    quotaLimitGb = quota?.limitInGb?.toFloat() ?: 1.0f
                )
            }
        }
    }

    fun setQuotaLimitEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isQuotaLimitEnabled = enabled)
        saveQuota()
    }

    fun setQuotaLimitGb(gb: Float) {
        _uiState.value = _uiState.value.copy(quotaLimitGb = gb)
        saveQuota()
    }

    private fun saveQuota() {
        viewModelScope.launch {
            val state = _uiState.value
            val bytes = (state.quotaLimitGb * 1024 * 1024 * 1024).toLong()
            quotaRepository.setAppQuota(uid, bytes, state.isQuotaLimitEnabled)
        }
    }
}
