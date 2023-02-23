package com.microtech.aidexx.utils.mmkv

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.ThresholdManager

object MmkvManager {

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

    fun saveToken(token: String) = MmkvUtil.encodeString(TOKEN, token)
    fun getToken(): String = MmkvUtil.decodeString(TOKEN, "")
    fun saveHypo(value: Float) = MmkvUtil.encodeFloat(HYPO, value)

    fun getHypo() = MmkvUtil.decodeFloat(HYPO, ThresholdManager.DEFAULT_HYPO)

    fun saveHyper(value: Float) = MmkvUtil.encodeFloat(HYPER, value)

    fun getHyper() = MmkvUtil.decodeFloat(HYPER, ThresholdManager.DEFAULT_HYPER)

    fun saveAppLaunched() = MmkvUtil.encodeBoolean(IS_APP_FIRST_LAUNCH, true)
    fun isAppFirstLaunch() = MmkvUtil.decodeBoolean(IS_APP_FIRST_LAUNCH, false)
    fun getUserId() = MmkvUtil.decodeString(GET_USER_ID, "")

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

    fun getCustomerServiceIconRight(right: Int) =
        MmkvUtil.decodeInt(CUSTOMER_SERVICE_ICON_RIGHT, 0)

    fun getCustomerServiceIconBottom() =
        MmkvUtil.decodeInt(CUSTOMER_SERVICE_ICON_BOTTOM, 0)
}