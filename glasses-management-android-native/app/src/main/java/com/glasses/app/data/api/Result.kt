package com.glasses.app.data.api

data class ApiResult<T>(
    val code: Int,
    val msg: String,
    val data: T?
)
