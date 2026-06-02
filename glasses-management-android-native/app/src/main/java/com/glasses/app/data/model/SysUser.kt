package com.glasses.app.data.model

data class SysUser(
    val id: Long? = null,
    val username: String? = null,
    val phone: String? = null,
    val realName: String? = null,
    val role: String? = null,
    val mustChangePassword: Boolean? = null,
    val disabled: Boolean? = null,
    val disabledTime: String? = null,
    val deleted: Boolean? = null,
    val deletedTime: String? = null,
    val createTime: String? = null
)
