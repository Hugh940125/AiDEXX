package com.microtech.aidexx.common.net.entity

open class ReqEntity

data class ReqChangePWDVerifyCode(
    val phoneNumber: String
): ReqEntity()

data class ReqChangePWD(
    val phoneNumber: String,
    val password: String,
    val verificationCode: String
): ReqEntity()