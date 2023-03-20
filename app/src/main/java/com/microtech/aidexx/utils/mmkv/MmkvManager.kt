package com.microtech.aidexx.utils.mmkv

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.ThresholdManager

object MmkvManager {

    private const val PHONE_NUMBER = "PHONE_NUMBER"
    private const val GET_USER_ID = "GET_USER_ID"
    private const val THEME = "THEME"
    private const val GLUCOSE_UNIT = "GLUCOSE_UNIT"
    private const val CUSTOMER_SERVICE_ICON_LEFT = "CUSTOMER_SERVICE_ICON_LEFT"
    private const val CUSTOMER_SERVICE_ICON_TOP = "CUSTOMER_SERVICE_ICON_TOP"
    private const val CUSTOMER_SERVICE_ICON_RIGHT = "CUSTOMER_SERVICE_ICON_RIGHT"
    private const val CUSTOMER_SERVICE_ICON_BOTTOM = "CUSTOMER_SERVICE_ICON_BOTTOM"
    private const val IS_APP_FIRST_LAUNCH = "IS_APP_FIRST_LAUNCH"
    private const val HYPER = "HYPER"
    private const val HYPO = "HYPO"
    private const val TOKEN = "TOKEN"
    private const val NOTICE_FREQUENCY = "NOTICE_FREQUENCY"
    private const val URGENT_NOTICE_FREQUENCY = "NOTICE_FREQUENCY"
    private const val HIGH_NOTICE_ENABLE = "HIGH_NOTICE_ENABLE"
    private const val LOW_NOTICE_ENABLE = "LOW_NOTICE_ENABLE"
    private const val URGENT_NOTICE_ENABLE = "URGENT_NOTICE_ENABLE"
    private const val FAST_DOWN_ALERT_ENABLE = "FALL_ALERT"
    private const val FAST_UP_ALERT_ENABLE = "RAISE_ALERT"
    private const val LAST_FAST_UP_ALERT_TIME = "LAST_FAST_UP_ALERT_TIME"
    private const val LAST_FAST_DOWN_ALERT_TIME = "LAST_FAST_DOWN_ALERT_TIME"
    private const val FLAG_LOGIN = "FLAG_LOGIN"
    private const val USER_AVATAR = "USER_AVATAR"

    fun saveProfile(profile: String) = MmkvUtil.encodeString(USER_AVATAR, profile)
    fun setLogin(isLogin: Boolean) = MmkvUtil.encodeBoolean(FLAG_LOGIN, isLogin)
    fun isLogin(): Boolean = MmkvUtil.decodeBoolean(FLAG_LOGIN, false)
    fun saveFastUpAlertTime(time: Long) = MmkvUtil.encodeLong(LAST_FAST_UP_ALERT_TIME, time)
    fun saveFastDownAlertTime(time: Long) = MmkvUtil.encodeLong(LAST_FAST_DOWN_ALERT_TIME, time)
    fun getLastFastDownAlertTime() = MmkvUtil.decodeLong(LAST_FAST_DOWN_ALERT_TIME, 0)
    fun getLastFastUpAlertTime() = MmkvUtil.decodeLong(LAST_FAST_UP_ALERT_TIME, 0)
    fun isFastUpAlertEnable() = MmkvUtil.decodeBoolean(FAST_UP_ALERT_ENABLE, true)
    fun isFastDownAlertEnable() = MmkvUtil.decodeBoolean(FAST_DOWN_ALERT_ENABLE, true)
    fun isUrgentAlertEnable() = MmkvUtil.decodeBoolean(URGENT_NOTICE_ENABLE, true)
    fun isHypoAlertEnable() = MmkvUtil.decodeBoolean(LOW_NOTICE_ENABLE, true)
    fun isHyperAlertEnable() = MmkvUtil.decodeBoolean(HIGH_NOTICE_ENABLE, true)
    fun getUrgentAlertFrequency() = MmkvUtil.decodeInt(URGENT_NOTICE_FREQUENCY, 0)
    fun getAlertFrequency() = MmkvUtil.decodeInt(NOTICE_FREQUENCY, 2)
    fun saveToken(token: String) = MmkvUtil.encodeString(TOKEN, token)
    fun getToken(): String = MmkvUtil.decodeString(TOKEN, "")
    fun saveHypo(value: Float) = MmkvUtil.encodeFloat(HYPO, value)
    fun getHypo() = MmkvUtil.decodeFloat(HYPO, ThresholdManager.DEFAULT_HYPO)

    fun saveHyper(value: Float) = MmkvUtil.encodeFloat(HYPER, value)

    fun getHyper() = MmkvUtil.decodeFloat(HYPER, ThresholdManager.DEFAULT_HYPER)

    fun saveAppLaunched() = MmkvUtil.encodeBoolean(IS_APP_FIRST_LAUNCH, true)
    fun isAppFirstLaunch() = MmkvUtil.decodeBoolean(IS_APP_FIRST_LAUNCH, false)
    fun getUserId() = MmkvUtil.decodeString(GET_USER_ID, "")
    fun saveUserId(id: String) = MmkvUtil.encodeString(GET_USER_ID, id)
    fun getPhone() = MmkvUtil.decodeString(PHONE_NUMBER, "")
    fun savePhone(phone: String) = MmkvUtil.encodeString(PHONE_NUMBER, phone)
    fun getOnlineServiceMsgCount() = MmkvUtil.decodeInt(UserInfoManager.instance().userId(), 0)

    fun setOnlineServiceMsgCount(count: Int) =
        MmkvUtil.encodeInt(UserInfoManager.instance().userId(), count)

    fun getEnquireFlag(flag: String): Boolean = MmkvUtil.decodeBoolean(flag, false)

    fun saveEnquireFlag(flag: String, value: Boolean) = MmkvUtil.encodeBoolean(flag, value)

    fun getTheme(): Int = MmkvUtil.decodeInt(THEME, 1)

    fun saveTheme(index: Int) = MmkvUtil.encodeInt(THEME, index)

    fun getGlucoseUnit() = MmkvUtil.decodeInt(GLUCOSE_UNIT, 1)

    fun saveGlucoseUnit(index: Int) = MmkvUtil.encodeInt(GLUCOSE_UNIT, index)
    fun saveCustomerServiceIconLeft(left: Int) =
        MmkvUtil.encodeInt(CUSTOMER_SERVICE_ICON_LEFT, left)

    fun saveCustomerServiceIconTop(top: Int) =
        MmkvUtil.encodeInt(CUSTOMER_SERVICE_ICON_TOP, top)

    fun saveCustomerServiceIconRight(right: Int) =
        MmkvUtil.encodeInt(CUSTOMER_SERVICE_ICON_RIGHT, right)

    fun saveCustomerServiceIconBottom(bottom: Int) =
        MmkvUtil.encodeInt(CUSTOMER_SERVICE_ICON_BOTTOM, bottom)

    fun getCustomerServiceIconLeft() =
        MmkvUtil.decodeInt(CUSTOMER_SERVICE_ICON_LEFT, 0)

    fun getCustomerServiceIconTop() =
        MmkvUtil.decodeInt(CUSTOMER_SERVICE_ICON_TOP, 0)

    fun getCustomerServiceIconRight() =
        MmkvUtil.decodeInt(CUSTOMER_SERVICE_ICON_RIGHT, 0)

    fun getCustomerServiceIconBottom() =
        MmkvUtil.decodeInt(CUSTOMER_SERVICE_ICON_BOTTOM, 0)
}