package com.quotatracker.app.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotatracker.app.data.local.db.dao.DailyTotalUsage
import com.quotatracker.app.data.repository.DataUsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val dailyTotals: List<DailyTotalUsage> = emptyList(),
    val isLoading: Boolean = true,
    val maxDailyUsage: Long = 0L
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dataUsageRepository: DataUsageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            dataUsageRepository.getDailyTotals(daysBack = 30).collect { list ->
                val max = list.maxOfOrNull { it.grandTotal } ?: 0L
                _uiState.value = _uiState.value.copy(
                    dailyTotals = list,
                    maxDailyUsage = max,
                    isLoading = false
                )
            }
        }
    }
}
