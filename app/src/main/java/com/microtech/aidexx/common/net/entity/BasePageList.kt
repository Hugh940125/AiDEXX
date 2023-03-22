package com.microtech.aidexx.common.net.entity

data class BasePageList<T>(
    val pageInfo: PageInfo = PageInfo(),
    val records: MutableList<T> = mutableListOf(),
    val deviceSn:String? = null,
    val startUp: Long? = null,
    val et: Int? = null
) {
    data class PageInfo(
        val currentPage: Int = 0,
        val pageSize: Int = 0,
        val sortOrder: String = "",
        val totalCount: Int = 0
    )
}