package com.glasses.native.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.native.data.model.RecycleBinData
import com.glasses.native.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecycleBinUiState(
    val data: RecycleBinData = RecycleBinData(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterType: String = "all",
    val showEmptyConfirm: Boolean = false,
    val showPurgeConfirm: Boolean = false,
    val purgingType: String? = null,
    val purgingId: Long? = null
)

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecycleBinUiState())
    val uiState: StateFlow<RecycleBinUiState> = _uiState

    init { loadRecycleBin() }

    fun loadRecycleBin() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = systemRepository.getRecycleBin(_uiState.value.filterType)
            result.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(data = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun onFilterChange(type: String) {
        _uiState.value = _uiState.value.copy(filterType = type)
        loadRecycleBin()
    }

    fun restore(type: String, id: Long) {
        viewModelScope.launch {
            systemRepository.restore(type, id).fold(
                onSuccess = { loadRecycleBin() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun showPurgeConfirm(type: String, id: Long) { _uiState.value = _uiState.value.copy(showPurgeConfirm = true, purgingType = type, purgingId = id) }
    fun hidePurgeConfirm() { _uiState.value = _uiState.value.copy(showPurgeConfirm = false, purgingType = null, purgingId = null) }

    fun purge() {
        val type = _uiState.value.purgingType ?: return
        val id = _uiState.value.purgingId ?: return
        viewModelScope.launch {
            systemRepository.purge(type, id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(showPurgeConfirm = false); loadRecycleBin() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun showEmptyConfirm() { _uiState.value = _uiState.value.copy(showEmptyConfirm = true) }
    fun hideEmptyConfirm() { _uiState.value = _uiState.value.copy(showEmptyConfirm = false) }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            systemRepository.emptyRecycleBin().fold(
                onSuccess = { _uiState.value = _uiState.value.copy(showEmptyConfirm = false); loadRecycleBin() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
