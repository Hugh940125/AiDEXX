package com.microtech.aidexx.common.net.entity

import com.microtech.aidexx.common.user.UserInfoManager
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

const val PAGE_SIZE = 1000

/**
 * data class转retrofit QueryMap
 */
fun <T : ReqEntity> T.toQueryMap(): Map<String, String> {
    return (this::class as KClass<T>).memberProperties.associate { prop ->
        prop.name to (prop.get(this)?.let { value ->
            if (value::class.isData) {
                ""
            } else {
                "$value"
            }
        } ?: "" )
    }
}

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

//region 数据事件
data class ReqGetCgmByPage(
    val pageNum: Int = 1,//	是 1 分页参数 页数(Integer)
    val pageSize: Int = PAGE_SIZE,//	是 100 分页参数 条数(Integer)
    val userId: String = UserInfoManager.instance().userId(),//	是 String (String)
    val autoIncrementColumn: Int?,//	否 0 自增列(Long).序号
): ReqEntity()
//endregion
