package com.microtech.aidexx.common.net.entity

import com.microtech.aidexx.BuildConfig
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

const val PAGE_SIZE = 500
const val CGM_RECENT_COUNT = 5000
const val BG_RECENT_COUNT = 5000
const val CAL_RECENT_COUNT = 5000
const val EVENT_RECENT_COUNT = 5000

/**
 * data class转retrofit QueryMap
 */
fun <T : ReqEntity> T.toQueryMap(): Map<String, String> {
    return (this::class as KClass<T>).memberProperties.associate { prop ->

        prop.get(this)?.let {
            prop.name to (if (it::class.isData) {
                ""
            } else {
                "$it"
            })
        } ?: ("" to "")
    }.toMutableMap().also { it.remove("") }
}

open class ReqEntity

//region 账号
data class ReqGetuiLogin(
    val getuiCid: String,
    val cidOrigin: String =
        when {
            BuildConfig.DEBUG -> "0"
            BuildConfig.FLAVOR_AREA == "cn" -> "1"
            BuildConfig.FLAVOR_AREA == "gp" -> "2"
            else -> "0"
        } , // 来源 0-测试 默认 1-国内

): ReqEntity()

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

open class ReqPageInfo(
    var pageNum: Int = 1, //	是 1 分页参数 页数(Integer)
    var pageSize: Int = PAGE_SIZE, //	是 100 分页参数 条数(Integer)
    var userId: String? = null //	是 String (String)
): ReqEntity()

//region 数据事件

open class ReqGetEventByPage(
    val startAutoIncrementColumn: Long? = null,//	否 0 自增列(Long).序号
    val endAutoIncrementColumn: Long? = null,//	否 0 自增列(Long).序号  结束点。闭区间
    val orderStrategy: String? = null //	否 ASC 枚举值.排序规则 默认DESC
): ReqPageInfo()

data class ReqSysPresetFoodPageInfo(
    val foodSysPresetId: Long?
): ReqPageInfo()
data class ReqSysPresetMedicationPageInfo(
    val medicationSysPresetId: Long?
): ReqPageInfo()
data class ReqSysPresetInsulinPageInfo(
    val insulinSysPresetId: Long?
): ReqPageInfo()
data class ReqSysPresetExercisePageInfo(
    val exerciseSysPresetId: Long?
): ReqPageInfo()

data class ReqSaveOrUpdateEventRecords<T>(
    val records: List<T>
): ReqEntity()

data class ReqDeleteEventIds (
    val ids: List<String>
): ReqEntity()

//endregion

//region 分享关注
data class ReqShareUserInfo (
    val readerAlias: String? = null, // 非必须 string 分享人给查看者设置的昵称 mock: @string
    val hideState: Int? = null, // 非必须 1 是否隐藏,隐藏同时关闭所有推送 mock: @integer
    val emergePushState: Int? = null, // 非必须 1 紧急推送开关 mock: @integer
    val readerUserName: String, // 必须 string 冗余得数据查看者用户名 mock: @string
    val normalPushState: Int? = null, // 非必须 1 普通是否推送 mock: @integer
): ReqEntity()

data class ReqModifyShareUserInfo (
    val providerAlias: String? = null, // 非必须 string 数据提供者的昵称
    val readerAlias: String? = null, // 非必须 string 数据查看者的昵称。
    val hideState: Int? = null, // 非必须 1 是否隐藏,隐藏同时关闭所有推送
    val emergePushState: Int? = null, // 非必须 1 紧急推送开关
    val normalPushState: Int? = null, // 非必须 1 普通是否推送
    val userAuthorizationId: String, // 必须 string 主键
): ReqEntity()

data class ReqGetShareOrFollowUsers(
    val type: String = "1"
): ReqGetEventByPage()

data class ReqGetFollowUserById(
    val authorizationId: String
): ReqGetEventByPage()
//endregion
