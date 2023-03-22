package com.microtech.aidexx.common.net.entity

const val RESULT_OK = 100000

data class BaseResponse<T>(
    var info: Info = Info(),
    var content: T,
    val code: Int = 0,
    val data: String = "",
    val msg: String = ""
) {
    data class Info(
        val code: Int = 0,
        var msg: String = ""
    )
}