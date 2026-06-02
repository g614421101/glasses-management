package com.glasses.native.data.model

data class Customer(
    val id: Long? = null,
    val name: String,
    val phone: String? = null,
    val gender: Int = 0,
    val birthday: String? = null,
    val remark: String? = null,
    val deleted: Boolean? = null,
    val deletedTime: String? = null,
    val deletedBy: Long? = null,
    val createTime: String? = null,
    val updateTime: String? = null
)

data class CustomerPageResponse(
    val records: List<Customer>,
    val total: Long,
    val size: Int,
    val current: Int,
    val pages: Int
)
