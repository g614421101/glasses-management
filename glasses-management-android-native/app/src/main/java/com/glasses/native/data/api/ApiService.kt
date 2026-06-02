package com.glasses.native.data.api

import com.glasses.native.data.model.LoginRequest
import com.glasses.native.data.model.RegisterRequest
import com.glasses.native.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResult<Map<String, Any>>>

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResult<String>>

    @GET("/api/auth/info")
    suspend fun getUserInfo(): Response<ApiResult<User>>

    @GET("/api/system/lan-info")
    suspend fun getLanInfo(): Response<ApiResult<Map<String, Any>>>
}
