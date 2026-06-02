package com.glasses.app.data.repository

import com.glasses.app.data.api.ApiService
import com.glasses.app.data.model.SalesStatsResponse
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getStats(
        startDate: String? = null,
        endDate: String? = null,
        page: Int = 1,
        size: Int = 10,
        showAll: Boolean = false
    ): Result<SalesStatsResponse> {
        return try {
            val response = apiService.getSalesStats(startDate, endDate, page, size, showAll)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取统计数据失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportRevenue(
        startDate: String? = null,
        endDate: String? = null,
        showAll: Boolean = false
    ): Result<ResponseBody> {
        return try {
            val response = apiService.exportRevenueExcel(startDate, endDate, showAll)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
