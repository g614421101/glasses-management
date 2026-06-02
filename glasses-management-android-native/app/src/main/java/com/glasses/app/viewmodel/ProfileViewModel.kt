package com.glasses.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glasses.app.data.repository.AuthRepository
import com.glasses.app.data.repository.SystemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val username: String = "",
    val phone: String = "",
    val realName: String = "",
    val role: String = "",
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null,
    val showChangePassword: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val systemRepository: SystemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = authRepository.getUserInfo()
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        username = user.username ?: "",
                        phone = user.phone ?: "",
                        realName = user.realName ?: "",
                        role = user.role ?: "",
                        isLoading = false
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun startEditing() { _uiState.value = _uiState.value.copy(isEditing = true) }
    fun cancelEditing() { _uiState.value = _uiState.value.copy(isEditing = false); loadProfile() }

    fun onUsernameChange(v: String) { _uiState.value = _uiState.value.copy(username = v) }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(phone = v) }
    fun onRealNameChange(v: String) { _uiState.value = _uiState.value.copy(realName = v) }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val s = _uiState.value
            systemRepository.updateProfile(s.username, s.phone, s.realName).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isEditing = false, isLoading = false, success = "个人资料已更新")
                    loadProfile()
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun showChangePassword() { _uiState.value = _uiState.value.copy(showChangePassword = true) }
    fun hideChangePassword() { _uiState.value = _uiState.value.copy(showChangePassword = false) }

    fun changePassword(old: String, new: String, confirm: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            systemRepository.changePassword(old, new, confirm).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(showChangePassword = false, isLoading = false, success = "密码已修改")
                },
                onFailure = { _uiState.value = _uiState.value.copy(isLoading = false, error = it.message) }
            )
        }
    }

    fun clearMessages() { _uiState.value = _uiState.value.copy(error = null, success = null) }
}
