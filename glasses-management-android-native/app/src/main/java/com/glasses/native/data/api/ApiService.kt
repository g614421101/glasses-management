package com.glasses.native.data.api

import com.glasses.native.data.model.Customer
import com.glasses.native.data.model.CustomerPageResponse
import com.glasses.native.data.model.LoginRequest
import com.glasses.native.data.model.OptometryRecord
import com.glasses.native.data.model.RegisterRequest
import com.glasses.native.data.model.SalesRecord
import com.glasses.native.data.model.SalesStatsResponse
import com.glasses.native.data.model.TimelineItem
import com.glasses.native.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResult<Map<String, Any>>>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResult<String>>

    @GET("/api/auth/info")
    suspend fun getUserInfo(): Response<ApiResult<User>>

    @GET("/api/system/lan-info")
    suspend fun getLanInfo(): Response<ApiResult<Map<String, Any>>>

    @GET("/api/customer/page")
    suspend fun getCustomerPage(
        @Query("keyword") keyword: String? = null,
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 10
    ): Response<ApiResult<CustomerPageResponse>>

    @POST("/api/customer/add")
    suspend fun addCustomer(@Body customer: Customer): Response<ApiResult<Any>>

    @PUT("/api/customer/update")
    suspend fun updateCustomer(@Body customer: Customer): Response<ApiResult<Boolean>>

    @DELETE("/api/customer/{id}")
    suspend fun deleteCustomer(@Path("id") id: Long): Response<ApiResult<Boolean>>

    @GET("/api/customer/{id}")
    suspend fun getCustomer(@Path("id") id: Long): Response<ApiResult<Customer>>

    // Archive
    @GET("/api/archive/{customerId}")
    suspend fun getArchiveTimeline(@Path("customerId") customerId: Long): Response<ApiResult<List<TimelineItem>>>

    // Optometry
    @POST("/api/optometry/add")
    suspend fun addOptometry(@Body record: OptometryRecord): Response<ApiResult<Boolean>>

    @PUT("/api/optometry/update")
    suspend fun updateOptometry(@Body record: OptometryRecord): Response<ApiResult<Boolean>>

    @DELETE("/api/optometry/{id}")
    suspend fun deleteOptometry(@Path("id") id: Long): Response<ApiResult<Boolean>>

    // Sales
    @POST("/api/sales/add")
    suspend fun addSales(@Body record: SalesRecord): Response<ApiResult<Boolean>>

    @PUT("/api/sales/update")
    suspend fun updateSales(@Body record: SalesRecord): Response<ApiResult<Boolean>>

    @DELETE("/api/sales/{id}")
    suspend fun deleteSales(@Path("id") id: Long): Response<ApiResult<Boolean>>

    // Stats
    @GET("/api/sales/stats")
    suspend fun getSalesStats(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("current") current: Int = 1,
        @Query("size") size: Int = 10,
        @Query("showAll") showAll: Boolean = false
    ): Response<ApiResult<SalesStatsResponse>>

    // Print
    @GET("/api/print/prescription/{recordId}")
    suspend fun getPrescriptionPdf(@Path("recordId") recordId: Long): okhttp3.ResponseBody

    @GET("/api/print/export/customer/{customerId}")
    suspend fun exportCustomerExcel(@Path("customerId") customerId: Long): okhttp3.ResponseBody

    @GET("/api/print/export/revenue")
    suspend fun exportRevenueExcel(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("showAll") showAll: Boolean = false
    ): okhttp3.ResponseBody
}
