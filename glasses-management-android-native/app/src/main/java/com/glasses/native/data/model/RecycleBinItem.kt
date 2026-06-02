package com.glasses.native.data.model

data class RecycleBinData(
    val customers: List<Customer> = emptyList(),
    val optometryRecords: List<OptometryRecord> = emptyList(),
    val salesRecords: List<SalesRecord> = emptyList()
)
