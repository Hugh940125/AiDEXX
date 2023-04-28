package com.microtech.aidexx.common.net.entity

open class ReqEntity

//region 账号
data class ReqPhoneVerCode(
    val phone: String
): ReqEntity()

data class ReqPhoneCodeLogin(
    val phone: String,
    val code: String,
    val password: String? = null
): ReqEntity()

data class ReqPwdLogin(
    val userName: String,
    val password: String
): ReqEntity()

data class ReqChangePWD(
    val userName: String,
    /** 小写32位MD5值 */
    val newPassword: String,
    val code: String
): ReqEntity()

data class ReqEmailRegister(
    val email: String,
    /** 小写32位MD5值 */
    val password: String,
    val code: String
): ReqEntity()

//endregion
