package com.glasses.native.data.repository

import com.glasses.native.data.api.ApiService
import com.glasses.native.data.model.Customer
import com.glasses.native.data.model.CustomerPageResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomerRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getCustomers(keyword: String? = null, page: Int = 1, size: Int = 10): Result<CustomerPageResponse> {
        return try {
            val response = apiService.getCustomerPage(keyword, page, size)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取顾客列表失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCustomer(customer: Customer): Result<Any> {
        return try {
            val response = apiService.addCustomer(customer)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data ?: "添加成功")
            } else {
                Result.failure(Exception(response.body()?.msg ?: "添加顾客失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCustomer(customer: Customer): Result<Boolean> {
        return try {
            val response = apiService.updateCustomer(customer)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "更新顾客失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCustomer(id: Long): Result<Boolean> {
        return try {
            val response = apiService.deleteCustomer(id)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "删除顾客失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCustomer(id: Long): Result<Customer> {
        return try {
            val response = apiService.getCustomer(id)
            if (response.isSuccessful && response.body()?.code == 200) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.msg ?: "获取顾客详情失败"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
