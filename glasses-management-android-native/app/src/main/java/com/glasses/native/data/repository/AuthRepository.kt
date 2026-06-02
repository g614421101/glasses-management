package com.glasses.native.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.glasses.native.data.api.ApiService
import com.glasses.native.data.api.AuthInterceptor
import com.glasses.native.data.model.LoginRequest
import com.glasses.native.data.model.RegisterRequest
import com.glasses.native.data.model.User
import com.glasses.native.di.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val ROLE_KEY = stringPreferencesKey("role")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val username: Flow<String?> = context.dataStore.data.map { it[USERNAME_KEY] }
    val role: Flow<String?> = context.dataStore.data.map { it[ROLE_KEY] }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            token.collect { cached ->
                AuthInterceptor.token = cached
            }
        }
    }

    suspend fun login(username: String, password: String): Result<Map<String, Any>> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body()?.code == 200) {
                val data = response.body()?.data ?: return Result.failure(Exception("服务器返回数据为空"))
                val token = data["token"] as? String ?: return Result.failure(Exception("未获取到 Token"))
                saveToken(token)
                AuthInterceptor.token = token
                (data["username"] as? String)?.let { saveUsername(it) }
                (data["role"] as? String)?.let { saveRole(it) }
                Result.success(data)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "登录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        inviteCode: String,
        username: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): Result<String> {
        return try {
            val response = apiService.register(
                RegisterRequest(inviteCode, username, phone, password, confirmPassword)
            )
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()?.data ?: "注册成功")
            } else {
                Result.failure(Exception(response.body()?.msg ?: "注册失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserInfo(): Result<User> {
        return try {
            val response = apiService.getUserInfo()
            if (response.isSuccessful && response.body()?.code == 200) {
                val user = response.body()?.data ?: return Result.failure(Exception("服务器返回数据为空"))
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取用户信息失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        AuthInterceptor.token = null
        context.dataStore.edit {
            it.remove(TOKEN_KEY)
            it.remove(USERNAME_KEY)
            it.remove(ROLE_KEY)
        }
    }

    private suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    private suspend fun saveUsername(username: String) {
        context.dataStore.edit { it[USERNAME_KEY] = username }
    }

    private suspend fun saveRole(role: String) {
        context.dataStore.edit { it[ROLE_KEY] = role }
    }
}
