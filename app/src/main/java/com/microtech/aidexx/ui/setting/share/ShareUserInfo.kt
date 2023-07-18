package com.microtech.aidexx.ui.setting.share

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.getContext
import com.microtech.aidexx.common.millisToHours
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.parseToTimestamp
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.UserEntity
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.toGlucoseValue
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Locale

data class UserTrendInfo(
    val appTime: String?,//	string 非必须 string 时间

    //number 非必须 趋势
    // -99 未知，无趋势 -3 快速下降 -2 下降
    // -1 缓慢下降 0 平稳 1 缓慢上升 2 上升 3 快速上升
    val trend: Int?,
    val trendValue: Int?, //number 非必须 趋势值。两位浮点数

    val trendId: String?,//	string 非必须 string 趋势表id
    val dstOffset: String?,//	string 非必须 string 偏移量
    val userId: String?,//	string 必须 string 用户id
    val appTimeZone: String?,//	string 非必须 string 时区
    val bloodGlucose: Int?, //	number 非必须 0.111111 值 mock: @float
): Parcelable {


    fun getGlucoseTrend(): DeviceModel.GlucoseTrend? =
        when (trend) {
            -3 -> DeviceModel.GlucoseTrend.SUPER_FAST_DOWN
            -2 -> DeviceModel.GlucoseTrend.FAST_DOWN
            -1 -> DeviceModel.GlucoseTrend.DOWN
            0 -> DeviceModel.GlucoseTrend.STEADY
            1 -> DeviceModel.GlucoseTrend.UP
            2 -> DeviceModel.GlucoseTrend.FAST_UP
            3 -> DeviceModel.GlucoseTrend.SUPER_FAST_UP
            else -> null
        }

    fun getGlucoseLevel(): DeviceModel.GlucoseLevel? =
        bloodGlucose?.let {
            if (it < ThresholdManager.hypo) {
                DeviceModel.GlucoseLevel.LOW
            } else if (it < ThresholdManager.hyper) {
                DeviceModel.GlucoseLevel.NORMAL
            } else {
                DeviceModel.GlucoseLevel.HIGH
            }
        }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appTime)
        parcel.writeValue(trend)
        parcel.writeValue(trendValue)
        parcel.writeString(trendId)
        parcel.writeString(dstOffset)
        parcel.writeString(userId)
        parcel.writeString(appTimeZone)
        parcel.writeValue(bloodGlucose)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserTrendInfo> {
        override fun createFromParcel(parcel: Parcel): UserTrendInfo {
            return UserTrendInfo(parcel)
        }

        override fun newArray(size: Int): Array<UserTrendInfo?> {
            return arrayOfNulls(size)
        }
    }


}

data class CgmDevice(
    val sensorIndex: Int?, // 非必 1
    val registerTime: String?, // 非必 2023-06-16 13:09:17 启用时间
    val startUpTimeZone: String?, // 必须 string
    val deviceKey: String?, // 必须 1 设备密钥文件
    val sensorStartUp: String?, // 必须 2023-06-16 13:09:17
    val dstOffset: String?, // 非必须 string 夏令时偏移量
    val deviceId: String?, // 必须 string 主键
    val userId: String?, // 非必须 string 外键:tbl_user.pk_id
    val deviceSn: String?, // 必须 string 设备sn
    val sensorId: String?, // 必须 string
    val et: Int?, // 非必须 1 传感器有效期
    val isForceReplace: Boolean?,// 非必须 false
    val unregisterTime: String?, // 非必须 2023-06-16 13:09:17 停用时间
    val deviceMac: String?, // 必须 string 设备mac地址
    val deviceModel: String?, // 必须 string 设备型号
): Parcelable {

    /**
     * null - 计算异常 隐藏界面或者--表示
     * < 0 - 传感器过期
     */
    fun getSensorLeftTime(): Int? {
        val et = et ?: 15
        var startUpTimestamp = sensorStartUp?.let {  startUpTime ->
            kotlin.runCatching {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.ENGLISH).parse(startUpTime)?.time
            }.getOrNull() ?: 0L
        } ?: 0L

        if (startUpTimestamp == 0L) {
            return null
        }
        return (et * TimeUtils.oneDayMillis - (System.currentTimeMillis() - startUpTimestamp)).millisToHours()
    }

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Boolean::class.java.classLoader) as? Boolean,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(sensorIndex)
        parcel.writeString(registerTime)
        parcel.writeString(startUpTimeZone)
        parcel.writeString(deviceKey)
        parcel.writeString(sensorStartUp)
        parcel.writeString(dstOffset)
        parcel.writeString(deviceId)
        parcel.writeString(userId)
        parcel.writeString(deviceSn)
        parcel.writeString(sensorId)
        parcel.writeValue(et)
        parcel.writeValue(isForceReplace)
        parcel.writeString(unregisterTime)
        parcel.writeString(deviceMac)
        parcel.writeString(deviceModel)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CgmDevice> {
        override fun createFromParcel(parcel: Parcel): CgmDevice {
            return CgmDevice(parcel)
        }

        override fun newArray(size: Int): Array<CgmDevice?> {
            return arrayOfNulls(size)
        }
    }

}

data class ShareUserInfo(
    var providerAlias: String? = null,// 非必须 string 数据提供者的昵称。在关注列表中展示
    var userTrend: UserTrendInfo? = null,//	object  非必须 备注:
    var readerAlias: String? = null,//	string 非必须 string 数据查看者的昵称，在分享列表中展示
    var dataReaderId: String? = null,//	string 必须 string 查看者userID
    var providerUserName: String? = null,//	string 非必须 string 冗余得数据提供者用户名，在关注列表中没有昵称展示此名称
    var hideState: Int? = null, //	integer 非必须 1 是否隐藏,隐藏同时关闭所有推送 
    var emergePushState: Int? = null, //	integer 非必须 1 紧急推送开关 
    var autoIncrementColumn: Int? = null, //	integer 必须 1 自增列 
    var readerUserName: String? = null,//	string 非必须 string 冗余得数据查看者用户名，在分享列表中没有昵称展示此名称
    var normalPushState: Int? = null, //	integer 非必须 1 普通是否推送 
    var dataProviderId: String? = null,//	string 必须 string 授权者userID
    var userAuthorizationId: String? = null,//	string 必须 string 主键
    var information: UserEntity? = null, // 用户信息
    var cgmDevice: CgmDevice? = null
): Parcelable {

    var isLooking = false

    val hide: Boolean
        get() = hideState == 1

    val emergePush: Boolean
        get() = emergePushState == 1

    val normalPush: Boolean
        get() = normalPushState == 1

    fun getDisplayName() = if (UserInfoManager.instance().userId() == dataReaderId) {
        providerAlias?.ifEmpty { null } ?: StringUtils.getMaskedPhone(providerUserName) ?: ""
    } else {
        readerAlias?.ifEmpty { null } ?: StringUtils.getMaskedPhone(readerUserName) ?: ""
    }

    fun getMaskedAccount() = if (UserInfoManager.instance().userId() == dataReaderId) {
         StringUtils.getMaskedPhone(providerUserName) ?: ""
    } else {
         StringUtils.getMaskedPhone(readerUserName) ?: ""
    }

    fun getAlias() = if (UserInfoManager.instance().userId() == dataReaderId) {
        providerAlias
    } else {
        readerAlias
    } ?: ""


    fun getGlucoseValue(): Float? = userTrend?.bloodGlucose?.toGlucoseValue()

    fun getLatestValueTimeStr(): String? {
        return userTrend?.appTime?.let {
            val timestamp = it.parseToTimestamp(userTrend?.appTimeZone!!)
            getFriendlyTimeSpanByNow(timestamp)
        } ?: getContext().getString(R.string.data_place_holder)
    }

    fun getSensorStatusDesc(): String {
        return cgmDevice?.getSensorLeftTime()?.let {
            if (it < 0) {
                getContext().getString(R.string.sensor_expired)
            } else if (it < TimeUtils.oneDayHour) {
                String.format(getContext().getString(R.string.expiring_in_hour), if (it < 1) "1" else "$it")
            } else {
                val days = BigDecimal(it).divide(
                    BigDecimal(TimeUtils.oneDayHour),
                    RoundingMode.CEILING
                ).toInt()
                String.format(getContext().getString(R.string.left_day), "$days")
            }
        } ?: String.format(getContext().getString(R.string.left_day), getContext().getString(R.string.data_place_holder))
    }


    private fun getFriendlyTimeSpanByNow(timestamp: Long?): String {
        return timestamp?.let {
            val minutesAgo = (TimeUtils.currentTimeMillis - it).millisToMinutes()
            return if (minutesAgo == 0) {
                getContext().getString(R.string.now)
            } else {
                buildString {
                    append(minutesAgo)
                    append(getContext().getString(R.string.min_ago))
                }
            }
        } ?: getContext().getString(R.string.data_place_holder)
    }

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(
                UserTrendInfo::javaClass.javaClass.classLoader,
                UserTrendInfo::class.java
            )
        } else {
            parcel.readParcelable(UserTrendInfo::javaClass.javaClass.classLoader)
        },
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(
                UserEntity::javaClass.javaClass.classLoader,
                UserEntity::class.java
            )
        } else {
            parcel.readParcelable(UserEntity::javaClass.javaClass.classLoader)
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            parcel.readParcelable(
                CgmDevice::javaClass.javaClass.classLoader,
                CgmDevice::class.java
            )
        } else {
            parcel.readParcelable(CgmDevice::javaClass.javaClass.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(providerAlias)
        parcel.writeParcelable(userTrend, flags)
        parcel.writeString(readerAlias)
        parcel.writeString(dataReaderId)
        parcel.writeString(providerUserName)
        parcel.writeValue(hideState)
        parcel.writeValue(emergePushState)
        parcel.writeValue(autoIncrementColumn)
        parcel.writeString(readerUserName)
        parcel.writeValue(normalPushState)
        parcel.writeString(dataProviderId)
        parcel.writeString(userAuthorizationId)
        parcel.writeParcelable(information, flags)
        parcel.writeParcelable(cgmDevice, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShareUserInfo> {
        override fun createFromParcel(parcel: Parcel): ShareUserInfo {
            return ShareUserInfo(parcel)
        }

        override fun newArray(size: Int): Array<ShareUserInfo?> {
            return arrayOfNulls(size)
        }
    }


}