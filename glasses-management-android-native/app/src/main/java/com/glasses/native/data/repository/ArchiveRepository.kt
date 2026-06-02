package com.glasses.native.data.repository

import com.glasses.native.data.api.ApiService
import com.glasses.native.data.model.OptometryRecord
import com.glasses.native.data.model.SalesRecord
import com.glasses.native.data.model.TimelineItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getTimeline(customerId: Long): Result<List<TimelineItem>> {
        return try {
            val response = apiService.getArchiveTimeline(customerId)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取档案失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addOptometry(record: OptometryRecord): Result<Boolean> {
        return try {
            val response = apiService.addOptometry(record)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "添加验光记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOptometry(record: OptometryRecord): Result<Boolean> {
        return try {
            val response = apiService.updateOptometry(record)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "更新验光记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteOptometry(id: Long): Result<Boolean> {
        return try {
            val response = apiService.deleteOptometry(id)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "删除验光记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSales(record: SalesRecord): Result<Boolean> {
        return try {
            val response = apiService.addSales(record)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "添加销售记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSales(record: SalesRecord): Result<Boolean> {
        return try {
            val response = apiService.updateSales(record)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "更新销售记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSales(id: Long): Result<Boolean> {
        return try {
            val response = apiService.deleteSales(id)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "删除销售记录失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPdf(recordId: Long): Result<okhttp3.ResponseBody> {
        return try {
            val response = apiService.getPrescriptionPdf(recordId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportExcel(customerId: Long): Result<okhttp3.ResponseBody> {
        return try {
            val response = apiService.exportCustomerExcel(customerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
