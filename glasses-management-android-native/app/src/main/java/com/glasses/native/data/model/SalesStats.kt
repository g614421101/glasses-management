package com.glasses.native.data.model

import java.math.BigDecimal

data class SalesStatsResponse(
    val totalRevenue: BigDecimal,
    val orderCount: Long,
    val records: SalesRecordPage
)

data class SalesRecordPage(
    val records: List<SalesRecord>,
    val total: Long,
    val size: Int,
    val current: Int,
    val pages: Int
)
