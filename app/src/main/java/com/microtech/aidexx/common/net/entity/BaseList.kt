package com.microtech.aidexx.common.net.entity

data class BaseList<T>(
    val records: MutableList<T> = mutableListOf()
)

