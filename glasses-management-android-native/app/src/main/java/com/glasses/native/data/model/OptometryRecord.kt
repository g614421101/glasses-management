package com.glasses.native.data.model

import java.math.BigDecimal

data class OptometryRecord(
    val id: Long? = null,
    val customerId: Long? = null,
    val customerName: String? = null,
    val odSph: BigDecimal? = null,
    val odCyl: BigDecimal? = null,
    val odAxis: Int? = null,
    val odVa: String? = null,
    val osSph: BigDecimal? = null,
    val osCyl: BigDecimal? = null,
    val osAxis: Int? = null,
    val osVa: String? = null,
    val odPd: BigDecimal? = null,
    val osPd: BigDecimal? = null,
    val pdFar: BigDecimal? = null,
    val pdNear: BigDecimal? = null,
    val addPower: BigDecimal? = null,
    val optometristName: String? = null,
    val remark: String? = null,
    val examDate: String? = null,
    val createTime: String? = null,
    val deleted: Boolean? = null
)
