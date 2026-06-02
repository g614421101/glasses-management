package com.glasses.native.data.api

data class ApiResult<T>(
    val code: Int,
    val msg: String,
    val data: T?
)
