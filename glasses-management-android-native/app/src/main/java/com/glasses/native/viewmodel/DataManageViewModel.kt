package com.glasses.native.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.native.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DataManageUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val importMode: String = "merge",
    val showResetConfirm: Boolean = false
)

@HiltViewModel
class DataManageViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DataManageUiState())
    val uiState: StateFlow<DataManageUiState> = _uiState

    fun exportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            systemRepository.exportData().fold(
                onSuccess = { body ->
                    val content = body.string()
                    _uiState.value = _uiState.value.copy(isLoading = false, message = "导出成功，数据大小: ${content.length} 字节")
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun setImportMode(mode: String) { _uiState.value = _uiState.value.copy(importMode = mode) }

    fun importData(file: File?) {
        if (file == null) {
            _uiState.value = _uiState.value.copy(message = "请先选择文件（文件选择功能需集成系统文件管理器）")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            systemRepository.importData(file, _uiState.value.importMode).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, message = "导入成功") },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun showResetConfirm() { _uiState.value = _uiState.value.copy(showResetConfirm = true) }
    fun hideResetConfirm() { _uiState.value = _uiState.value.copy(showResetConfirm = false) }

    fun resetData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showResetConfirm = false)
            systemRepository.resetData().fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isLoading = false, message = it) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun clearMessage() { _uiState.value = _uiState.value.copy(message = null, error = null) }
}
