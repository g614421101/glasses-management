package com.glasses.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.app.data.model.SalesRecord
import com.glasses.app.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StatsUiState(
    val totalRevenue: BigDecimal = BigDecimal.ZERO,
    val orderCount: Long = 0,
    val records: List<SalesRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAll: Boolean = false,
    val startDate: String = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
    val endDate: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val pageSize: Int = 10
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        loadStats()
    }

    fun loadStats(page: Int = 1) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val state = _uiState.value
            val result = statsRepository.getStats(
                startDate = if (state.showAll) null else state.startDate,
                endDate = if (state.showAll) null else state.endDate,
                page = page,
                size = state.pageSize,
                showAll = state.showAll
            )
            result.fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        totalRevenue = data.totalRevenue,
                        orderCount = data.orderCount,
                        records = data.records.records,
                        isLoading = false,
                        currentPage = data.records.current,
                        totalPages = data.records.pages
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun onShowAllChange(showAll: Boolean) {
        _uiState.value = _uiState.value.copy(showAll = showAll)
        loadStats(1)
    }

    fun onDateChange(startDate: String, endDate: String) {
        _uiState.value = _uiState.value.copy(startDate = startDate, endDate = endDate)
        loadStats(1)
    }

    fun onNextPage() {
        if (_uiState.value.currentPage < _uiState.value.totalPages) {
            loadStats(_uiState.value.currentPage + 1)
        }
    }

    fun onPrevPage() {
        if (_uiState.value.currentPage > 1) {
            loadStats(_uiState.value.currentPage - 1)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        loadStats(_uiState.value.currentPage)
    }
}
