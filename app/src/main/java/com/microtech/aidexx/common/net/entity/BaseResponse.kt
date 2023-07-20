package com.microtech.aidexx.common.net.entity

const val RESULT_OK = 200

data class BaseResponse<T>(
    val code: Int = 0,
    val msg: String = "",
    var data: T?
)

//region 账号
data class ResLogin(
    val userId: String,
    val phone: String?,
    val email: String?,
    val token: String
)

data class ResUserInfo(
    val userInformationId: String?, //非必须
    val email: String?,
    val phone: String?, // 非必须
    val avatar: String?, // 非必须 头像地址
    val userId: String?, //必须
    val name: String?, //非必须
    val surname: String?, //非必须
    val middleName: String?, //非必须
    val givenName: String?, //非必须
    val gender: Int?, // 非必须 1男2女
    val birthDate: String?,// 非必须 UTC格式时间，生日
    val height: Int?, // 非必须
    val bodyWeight: Int?, //非必须
    val diabetesType: Int?, // 非必须 疾病类型
    val diagnosisDate: String?, //非必须 UTC格式时间
    val complications: String?, // 非必须 并发症(多选); 0无,1心血管并发症,2视网膜病变,3神经病变,4肾病,5糖尿病足,99其他)
)
//endregion


data class UpgradeInfo(
    val appUpdateInfo: VersionInfo?,
    val resourceUpdateInfo: VersionInfo?,
) {
    data class VersionInfo(
        val isForce: Boolean,
        val info: VersionData
    )

    data class VersionData(
        val version: String = "",
        val downloadpath: String = "", // 接口返回时为下载地址 下载成功后保存本地地址
        val description: String = "",
        val force: Int = 0,
        val configId: Int = 0,
        val sha256: String = ""
    )
}

data class ResEventPresetVersion(
    val sysVersion: String?,
    val userVersion: String?
)

data class TrendInfo(
    val appTime: String,
    val trend: Int,
    val trendValue: Int,
    val trendId: String,
    val dstOffset: String,
    val userId: String,
    val appTimeZone: String
)

data class WelfareInfo(
    val viewWelfareCenter: Boolean,
    val viewIndexTag: Boolean,
    val viewIndexBanner: Boolean,
    val activityList: List<ActivityInfo>
)

data class ActivityInfo(
    val title: String,
    val content: String,
    val activityId: Int,
    val url: String,
    val description: String,
    val isLook: Int,
)