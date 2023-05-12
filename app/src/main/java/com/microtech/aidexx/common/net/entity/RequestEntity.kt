package com.microtech.aidexx.common.net.entity

import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

const val PAGE_SIZE = 1000
const val CGM_RECENT_COUNT = 5000

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
    val userId: String?,//	是 String (String)
    val startAutoIncrementColumn: Long?,//	否 0 自增列(Long).序号
    val endAutoIncrementColumn: Long?,//	否 0 自增列(Long).序号  结束点。闭区间
    val orderStrategy: String? //	否 ASC 枚举值.排序规则 默认DESC
): ReqEntity()

data class ReqGetBgByPage(
    val pageNum: Int = 1,//	是 1 分页参数 页数(Integer)
    val pageSize: Int = PAGE_SIZE,//	是 100 分页参数 条数(Integer)
    val date: String?,//	筛选时间。目前只支持时间，取最后一条数据的createTime
    val userId: String?,//	是 String (String)
): ReqEntity()
//endregion
