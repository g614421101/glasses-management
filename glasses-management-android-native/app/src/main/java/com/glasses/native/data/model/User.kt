package com.glasses.native.data.model

data class User(
    val id: Long? = null,
    val username: String? = null,
    val phone: String? = null,
    val realName: String? = null,
    val role: String? = null,
    val mustChangePassword: Boolean? = null,
    val disabled: Boolean? = null,
    val deleted: Boolean? = null,
    val createTime: String? = null
)
