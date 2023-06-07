package com.microtech.aidexx.utils.mmkv

import com.microtech.aidexx.common.getStartOfTheDay
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.DataSyncController
import com.microtech.aidexx.ui.main.event.viewmodels.EventType
import com.microtech.aidexx.utils.ThresholdManager
import java.util.Date

object MmkvManager {

    private const val SIGNAL_NOTICE_FREQUENCY = "SIGNAL_NOTICE_FREQUENCY"
    private const val SIGNAL_NOTICE_METHOD = "SIGNAL_NOTICE_METHOD"
    private const val NOTICE_METHOD = "NOTICE_METHOD"
    private const val URGENT_NOTICE_METHOD = "URGENT_NOTICE_METHOD"
    private const val PHONE_NUMBER = "mobile_num"
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
    private const val APP_CHECK_UPDATE_DATE = "APP_CHECK_UPDATE_DATE"
    private const val NickName = "NickName"
    private const val SurName = "SurName"
    private const val GivenName = "GivenName"
    private const val ALREADY_SHOW_FOLLOWERS_DIALOG_GUIDE = "already_show_dialog_guide"
    private const val PRESET_VERSION_ = "PRESET_VERSION_"
    private const val UNIT_LATEST_UPDATE_TIME = "UNIT_LATEST_UPDATE_TIME"
    private const val UNIT_VERSION = "UNIT_VERSION"
    private const val UNIT_LOADED_APK_VERSION = "UNIT_LOADED_APK_VERSION"
    private const val RESOURCE_VERSION = "RESOURCE_VERSION"

    fun setResourceVersion(version: String) = MmkvUtil.encodeString(RESOURCE_VERSION, version)
    fun getResourceVersion(): String = MmkvUtil.decodeString(RESOURCE_VERSION, "")
    fun setLastLoginEventDownloadState(key: String, isSuccess: Boolean) = MmkvUtil.encodeBoolean(key, isSuccess)
    fun isLastLoginEventDownloadSuccess(key: String): Boolean = MmkvUtil.decodeBoolean(key, true)

    fun setEventSyncTask(key: String, tasks: DataSyncController.SyncTaskItemList?) {
        MmkvUtil.encodeString(key, tasks?.toString()?:"")
    }
    fun getEventSyncTask(key: String): DataSyncController.SyncTaskItemList? =
        DataSyncController.SyncTaskItemList.fromString(
            MmkvUtil.decodeString(key, "")
        )
    fun setEventDataId(key: String, eventId: Long?) {
        MmkvUtil.encodeLong(key, eventId ?: -1L)
    }
    fun getEventDataId(key: String): Long? {
        val id = MmkvUtil.decodeLong(key, -1L)
        return if (id == -1L) null else id
    }

    fun getUnitVersion() = MmkvUtil.decodeInt(UNIT_VERSION, 0)
    fun setUnitVersion(v: Int) = MmkvUtil.encodeInt(UNIT_VERSION, v)

    fun getUnitLoadedApkVersion() = MmkvUtil.decodeInt(UNIT_LOADED_APK_VERSION, 0)
    fun setUnitLoadedApkVersion(v: Int) = MmkvUtil.encodeInt(UNIT_LOADED_APK_VERSION, v)

    fun getUnitLatestUpdateTime() = MmkvUtil.decodeLong(UNIT_LATEST_UPDATE_TIME, 0)
    fun setUnitLatestUpdateTime(time: Long) = MmkvUtil.encodeLong(UNIT_LATEST_UPDATE_TIME, time)

    fun setPresetVersion(@EventType type: Int, version: String, isSys: Boolean) {
        return MmkvUtil.encodeString("${PRESET_VERSION_}$type${if (isSys) "_SYS" else "_USR"}", version)
    }
    fun getPresetVersion(@EventType type: Int, isSys: Boolean): String {
        return MmkvUtil.decodeString("${PRESET_VERSION_}$type${if (isSys) "_SYS" else "_USR"}", "")
    }

    fun setAlreadyShowFollowersGuide() = MmkvUtil.encodeBoolean(ALREADY_SHOW_FOLLOWERS_DIALOG_GUIDE, true)
    fun isAlreadyShowFollowersGuide() = MmkvUtil.decodeBoolean(ALREADY_SHOW_FOLLOWERS_DIALOG_GUIDE, false)

    fun saveNickName(name: String) = MmkvUtil.encodeString(NickName, name)
    fun getNickName(default: String = "") = MmkvUtil.decodeString(NickName, default)
    fun saveSurName(name: String) = MmkvUtil.encodeString(SurName, name)
    fun getSurName(default: String = "") = MmkvUtil.decodeString(SurName, default)
    fun saveGivenName(name: String) = MmkvUtil.encodeString(GivenName, name)
    fun getGivenName(default: String = "") = MmkvUtil.decodeString(GivenName, default)
    fun saveProfile(profile: String) = MmkvUtil.encodeString(USER_AVATAR, profile)
    fun setLogin(isLogin: Boolean) = MmkvUtil.encodeBoolean(FLAG_LOGIN, isLogin)
    fun isLogin(): Boolean = MmkvUtil.decodeBoolean(FLAG_LOGIN, false)
    fun saveFastUpAlertTime(time: Long) = MmkvUtil.encodeLong(LAST_FAST_UP_ALERT_TIME, time)
    fun saveFastDownAlertTime(time: Long) = MmkvUtil.encodeLong(LAST_FAST_DOWN_ALERT_TIME, time)
    fun getLastFastDownAlertTime() = MmkvUtil.decodeLong(LAST_FAST_DOWN_ALERT_TIME, 0)
    fun getLastFastUpAlertTime() = MmkvUtil.decodeLong(LAST_FAST_UP_ALERT_TIME, 0)
    fun isFastUpAlertEnable() = MmkvUtil.decodeBoolean(FAST_UP_ALERT_ENABLE, true)
    fun setFastUpAlertEnable(enable: Boolean) = MmkvUtil.encodeBoolean(FAST_UP_ALERT_ENABLE, enable)
    fun isFastDownAlertEnable() = MmkvUtil.decodeBoolean(FAST_DOWN_ALERT_ENABLE, true)
    fun setFastDownAlertEnable(enable: Boolean) =
        MmkvUtil.encodeBoolean(FAST_DOWN_ALERT_ENABLE, enable)

    fun isUrgentAlertEnable() = MmkvUtil.decodeBoolean(URGENT_NOTICE_ENABLE, true)
    fun setUrgentAlertEnable(enable: Boolean) = MmkvUtil.encodeBoolean(URGENT_NOTICE_ENABLE, enable)
    fun isHypoAlertEnable() = MmkvUtil.decodeBoolean(LOW_NOTICE_ENABLE, true)
    fun setHypoAlertEnable(enable: Boolean) = MmkvUtil.encodeBoolean(LOW_NOTICE_ENABLE, enable)
    fun isHyperAlertEnable() = MmkvUtil.decodeBoolean(HIGH_NOTICE_ENABLE, true)
    fun setHyperAlertEnable(enable: Boolean) = MmkvUtil.encodeBoolean(HIGH_NOTICE_ENABLE, enable)
    fun getUrgentAlertMethod() = MmkvUtil.decodeInt(URGENT_NOTICE_METHOD, 2)
    fun getAlertMethod() = MmkvUtil.decodeInt(NOTICE_METHOD, 2)
    fun saveUrgentAlertMethod(method: Int) = MmkvUtil.encodeInt(URGENT_NOTICE_METHOD, method)
    fun saveAlertMethod(method: Int) = MmkvUtil.encodeInt(NOTICE_METHOD, method)
    fun getUrgentAlertFrequency() = MmkvUtil.decodeInt(URGENT_NOTICE_FREQUENCY, 0)
    fun getAlertFrequency() = MmkvUtil.decodeInt(NOTICE_FREQUENCY, 2)
    fun saveUrgentAlertFrequency(frequency: Int) =
        MmkvUtil.encodeInt(URGENT_NOTICE_FREQUENCY, frequency)

    fun saveAlertFrequency(frequency: Int) = MmkvUtil.encodeInt(NOTICE_FREQUENCY, frequency)
    fun saveToken(token: String) = MmkvUtil.encodeString(TOKEN, token)
    fun getToken(): String = MmkvUtil.decodeString(TOKEN, "")
    fun saveHypo(value: Float) = MmkvUtil.encodeFloat(HYPO, value)
    fun getHypo() = MmkvUtil.decodeFloat(HYPO, ThresholdManager.DEFAULT_HYPO)

    fun saveHyper(value: Float) = MmkvUtil.encodeFloat(HYPER, value)

    fun getHyper() = MmkvUtil.decodeFloat(HYPER, ThresholdManager.DEFAULT_HYPER)

    fun saveAppLaunched() = MmkvUtil.encodeBoolean(IS_APP_FIRST_LAUNCH, false)
    fun isAppFirstLaunch() = MmkvUtil.decodeBoolean(IS_APP_FIRST_LAUNCH, true)
    fun getUserId() = MmkvUtil.decodeString(GET_USER_ID, "")
    fun saveUserId(id: String) = MmkvUtil.encodeString(GET_USER_ID, id)
    fun getPhone() = MmkvUtil.decodeString(PHONE_NUMBER, "")
    fun savePhone(phone: String) = MmkvUtil.encodeString(PHONE_NUMBER, phone)
    fun getOnlineServiceMsgCount() = MmkvUtil.decodeInt(UserInfoManager.instance().userId(), 0)

    fun setOnlineServiceMsgCount(count: Int) =
        MmkvUtil.encodeInt(UserInfoManager.instance().userId(), count)

    fun getEnquireFlag(flag: String): Boolean = MmkvUtil.decodeBoolean(flag, false)

    fun saveEnquireFlag(flag: String, value: Boolean) = MmkvUtil.encodeBoolean(flag, value)

    fun getTheme(): Int = MmkvUtil.decodeInt(THEME, 0)

    fun saveTheme(index: Int) = MmkvUtil.encodeInt(THEME, index)

    fun getGlucoseUnit() = MmkvUtil.decodeInt(GLUCOSE_UNIT, 0)

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

    fun updateAppCheckVersionTime(date: Long = Date().getStartOfTheDay().time) =
        MmkvUtil.encodeLong(APP_CHECK_UPDATE_DATE, date)

    fun getAppCheckVersionTime(): Long =
        MmkvUtil.decodeLong(APP_CHECK_UPDATE_DATE, 0)

    fun signalLossAlertMethod(): Int = MmkvUtil.decodeInt(SIGNAL_NOTICE_METHOD, 2)

    fun setSignalLossMethod(index: Int) = MmkvUtil.encodeInt(SIGNAL_NOTICE_METHOD, index)
    fun signalLossAlertFrequency(): Int = MmkvUtil.decodeInt(SIGNAL_NOTICE_FREQUENCY, 0)
    fun setSignalLossAlertFrequency(index: Int) = MmkvUtil.encodeInt(SIGNAL_NOTICE_FREQUENCY, index)
}