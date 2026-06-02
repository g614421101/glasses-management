package com.glasses.native.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.native.data.model.SysUser
import com.glasses.native.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SysUserUiState(
    val users: List<SysUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val includeDeleted: Boolean = false,
    val showResetPasswordDialog: Boolean = false,
    val resetPasswordResult: String? = null,
    val showDeleteConfirm: Boolean = false,
    val deletingUser: SysUser? = null,
    val showPurgeConfirm: Boolean = false,
    val purgingUser: SysUser? = null
)

@HiltViewModel
class SysUserViewModel @Inject constructor(
    private val systemRepository: SystemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SysUserUiState())
    val uiState: StateFlow<SysUserUiState> = _uiState

    init { loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = systemRepository.getUsers(_uiState.value.includeDeleted)
            result.fold(
                onSuccess = { _uiState.value = _uiState.value.copy(users = it, isLoading = false) },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun toggleIncludeDeleted() {
        _uiState.value = _uiState.value.copy(includeDeleted = !_uiState.value.includeDeleted)
        loadUsers()
    }

    fun disableUser(id: Long) {
        viewModelScope.launch {
            systemRepository.disableUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun enableUser(id: Long) {
        viewModelScope.launch {
            systemRepository.enableUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun showDeleteConfirm(user: SysUser) { _uiState.value = _uiState.value.copy(showDeleteConfirm = true, deletingUser = user) }
    fun hideDeleteConfirm() { _uiState.value = _uiState.value.copy(showDeleteConfirm = false, deletingUser = null) }

    fun deleteUser() {
        val user = _uiState.value.deletingUser ?: return
        viewModelScope.launch {
            systemRepository.deleteUser(user.id!!).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(showDeleteConfirm = false); loadUsers() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun restoreUser(id: Long) {
        viewModelScope.launch {
            systemRepository.restoreUser(id).fold(
                onSuccess = { loadUsers() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun showPurgeConfirm(user: SysUser) { _uiState.value = _uiState.value.copy(showPurgeConfirm = true, purgingUser = user) }
    fun hidePurgeConfirm() { _uiState.value = _uiState.value.copy(showPurgeConfirm = false, purgingUser = null) }

    fun purgeUser() {
        val user = _uiState.value.purgingUser ?: return
        viewModelScope.launch {
            systemRepository.purgeUser(user.id!!).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(showPurgeConfirm = false); loadUsers() },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun resetPassword(id: Long) {
        viewModelScope.launch {
            systemRepository.resetPassword(id).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(showResetPasswordDialog = true, resetPasswordResult = it) },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun hideResetPasswordDialog() { _uiState.value = _uiState.value.copy(showResetPasswordDialog = false, resetPasswordResult = null) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
