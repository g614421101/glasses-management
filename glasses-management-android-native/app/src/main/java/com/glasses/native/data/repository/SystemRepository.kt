package com.glasses.native.data.repository

import com.glasses.native.data.api.ApiService
import com.glasses.native.data.model.RecycleBinData
import com.glasses.native.data.model.SysUser
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemRepository @Inject constructor(
    private val apiService: ApiService
) {
    // User management
    suspend fun getUsers(includeDeleted: Boolean = false): Result<List<SysUser>> {
        return try {
            val response = apiService.getSysUserList(includeDeleted)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: emptyList())
            } else Result.failure(Exception(response.body()?.msg ?: "获取用户列表失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun disableUser(id: Long): Result<Boolean> {
        return try {
            val response = apiService.disableUser(id)
            if (response.isSuccessful && response.body()?.code == 200) Result.success(true)
            else Result.failure(Exception(response.body()?.msg ?: "禁用失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun enableUser(id: Long): Result<Boolean> {
        return try {
            val response = apiService.enableUser(id)
            if (response.isSuccessful && response.body()?.code == 200) Result.success(true)
            else Result.failure(Exception(response.body()?.msg ?: "启用失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteUser(id: Long): Result<Boolean> {
        return try {
            val response = apiService.deleteUser(id)
            if (response.isSuccessful && response.body()?.code == 200) Result.success(true)
            else Result.failure(Exception(response.body()?.msg ?: "删除失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun restoreUser(id: Long): Result<Boolean> {
        return try {
            val response = apiService.restoreUser(id)
            if (response.isSuccessful && response.body()?.code == 200) Result.success(true)
            else Result.failure(Exception(response.body()?.msg ?: "恢复失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun purgeUser(id: Long): Result<Boolean> {
        return try {
            val response = apiService.purgeUser(id)
            if (response.isSuccessful && response.body()?.code == 200) Result.success(true)
            else Result.failure(Exception(response.body()?.msg ?: "彻底删除失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun resetPassword(id: Long): Result<String> {
        return try {
            val response = apiService.resetPassword(id)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: "密码已重置")
            } else Result.failure(Exception(response.body()?.msg ?: "重置密码失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Recycle bin
    suspend fun getRecycleBin(type: String = "all"): Result<RecycleBinData> {
        return try {
            val response = apiService.getRecycleBin(type)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: RecycleBinData())
            } else Result.failure(Exception(response.body()?.msg ?: "获取回收站失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun restore(type: String, id: Long): Result<Boolean> {
        return try {
            val response = apiService.restoreFromRecycleBin(type, id)
            if (response.isSuccessful && response.body()?.code == 200) Result.success(true)
            else Result.failure(Exception(response.body()?.msg ?: "恢复失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun purge(type: String, id: Long): Result<Boolean> {
        return try {
            val response = apiService.purgeFromRecycleBin(type, id)
            if (response.isSuccessful && response.body()?.code == 200) Result.success(true)
            else Result.failure(Exception(response.body()?.msg ?: "彻底删除失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun emptyRecycleBin(): Result<Map<String, Int>> {
        return try {
            val response = apiService.emptyRecycleBin()
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: emptyMap())
            } else Result.failure(Exception(response.body()?.msg ?: "清空失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Data management
    suspend fun exportData(): Result<ResponseBody> {
        return try { Result.success(apiService.exportAllData()) }
        catch (e: Exception) { Result.failure(e) }
    }

    suspend fun importData(file: File, mode: String): Result<Map<String, Any>> {
        return try {
            val filePart = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("application/json".toMediaTypeOrNull()))
            val modePart = mode.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiService.importData(filePart, modePart)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: emptyMap())
            } else Result.failure(Exception(response.body()?.msg ?: "导入失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun resetData(): Result<String> {
        return try {
            val response = apiService.resetData()
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: "数据已重置")
            } else Result.failure(Exception(response.body()?.msg ?: "重置失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    // Profile
    suspend fun updateProfile(username: String, phone: String, realName: String): Result<Map<String, Any>> {
        return try {
            val body = mapOf("username" to username, "phone" to phone, "realName" to realName)
            val response = apiService.updateProfile(body)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: emptyMap())
            } else Result.failure(Exception(response.body()?.msg ?: "更新失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String): Result<Boolean> {
        return try {
            val body = mapOf("oldPassword" to oldPassword, "newPassword" to newPassword, "confirmPassword" to confirmPassword)
            val response = apiService.changePassword(body)
            if (response.isSuccessful && response.body()?.code == 200) Result.success(true)
            else Result.failure(Exception(response.body()?.msg ?: "修改密码失败"))
        } catch (e: Exception) { Result.failure(e) }
    }
}
