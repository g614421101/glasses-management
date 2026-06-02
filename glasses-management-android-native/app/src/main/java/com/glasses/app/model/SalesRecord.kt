package com.glasses.app.data.model

import java.math.BigDecimal

data class SalesRecord(
    val id: Long? = null,
    val recordNo: String? = null,
    val customerId: Long? = null,
    val optometryId: Long? = null,
    val customerName: String? = null,
    val frameBrand: String? = null,
    val frameModel: String? = null,
    val frameQuantity: Int? = null,
    val frameRetailPrice: BigDecimal? = null,
    val framePrice: BigDecimal? = null,
    val lensBrand: String? = null,
    val lensParams: String? = null,
    val lensQuantity: Int? = null,
    val lensRetailPrice: BigDecimal? = null,
    val lensPrice: BigDecimal? = null,
    val totalRetailPrice: BigDecimal? = null,
    val totalAmount: BigDecimal? = null,
    val remark: String? = null,
    val salesDate: String? = null,
    val operatorId: Long? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val deleted: Boolean? = null
)
