package com.microtech.aidexx.utils.mmkv

import android.os.Parcelable
import com.microtech.aidexx.BuildConfig
import com.microtech.aidexx.common.getStartOfTheDay
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.DataSyncController
import com.microtech.aidexx.db.entity.SettingsEntity
import com.microtech.aidexx.db.entity.event.preset.BaseSysPreset
import com.microtech.aidexx.db.entity.event.preset.DietSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.InsulinSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.MedicineSysPresetEntity
import com.microtech.aidexx.db.entity.event.preset.SportSysPresetEntity
import com.microtech.aidexx.ui.main.event.viewmodels.EventType
import com.microtech.aidexx.utils.ThresholdManager
import java.util.Date
import java.util.Locale

object MmkvManager {
    private const val SETTINGS = "SETTINGS"
    private const val PHONE_NUMBER = "PHONE_NUMBER"
    private const val USER_ID = "USER_ID"
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
    private const val FLAG_LOGIN = "FLAG_LOGIN"
    private const val USER_AVATAR = "USER_AVATAR"
    private const val APP_CHECK_UPDATE_DATE = "APP_CHECK_UPDATE_DATE"
    private const val NICK_NAME = "NICK_NAME"
    private const val SUR_NAME = "SUR_NAME"
    private const val GIVEN_NAME = "GIVEN_NAME"
    private const val ALREADY_SHOW_FOLLOWERS_DIALOG_GUIDE = "ALREADY_SHOW_FOLLOWERS_DIALOG_GUIDE"
    private const val PRESET_VERSION_ = "PRESET_VERSION_"
    private const val UNIT_LATEST_UPDATE_TIME = "UNIT_LATEST_UPDATE_TIME"
    private const val UNIT_LOADED_APK_VERSION = "UNIT_LOADED_APK_VERSION"
    private const val RESOURCE_VERSION = "RESOURCE_VERSION"
    private const val VERSION_UNIT = "VERSION_UNIT"
    private const val FLAG_NEW_VERSION_UNIT = "FLAG_NEW_VERSION_UNIT"
    private const val VERSION_LANGUAGE = "VERSION_LANGUAGE"
    private const val FLAG_NEW_VERSION_LANGUAGE = "FLAG_NEW_VERSION_LANGUAGE"
    private const val VERSION_FOOD_SYS_PRESET = "VERSION_FOOD_SYS_PRESET"
    private const val VERSION_EXERCISE_SYS_PRESET = "VERSION_EXERCISE_SYS_PRESET"
    private const val VERSION_MEDICINE_SYS_PRESET = "VERSION_MEDICINE_SYS_PRESET"
    private const val VERSION_INSULIN_SYS_PRESET = "VERSION_INSULIN_SYS_PRESET"
    private const val FLAG_NEW_VERSION_FOOD_SYS_PRESET = "FLAG_NEW_VERSION_FOOD_SYS_PRESET"
    private const val FLAG_NEW_VERSION_EXERCISE_SYS_PRESET = "FLAG_NEW_VERSION_EXERCISE_SYS_PRESET"
    private const val FLAG_NEW_VERSION_MEDICINE_SYS_PRESET = "FLAG_NEW_VERSION_MEDICINE_SYS_PRESET"
    private const val FLAG_NEW_VERSION_INSULIN_SYS_PRESET = "FLAG_NEW_VERSION_INSULIN_SYS_PRESET"
    private const val CURRENT_LANGUAGE_TAG = "CURRENT_LANGUAGE_TAG"
    private const val UPGRADE_RESOURCE_ZIP_FILE_INFO = "UPGRADE_RESOURCE_ZIP_FILE_INFO"
    private const val HAS_SHOW_AVATAR_ENQUIRE = "HAS_SHOW_AVATAR_ENQUIRE"


    fun setUpgradeResourceZipFileInfo(info: String) =
        MmkvUtil.encodeString(UPGRADE_RESOURCE_ZIP_FILE_INFO, info)

    fun getUpgradeResourceZipFileInfo(): String =
        MmkvUtil.decodeString(UPGRADE_RESOURCE_ZIP_FILE_INFO, "")

    fun setCurrentLanguageTag(tag: String) = MmkvUtil.encodeString(CURRENT_LANGUAGE_TAG, tag)
    fun getCurrentLanguageTag() =
        MmkvUtil.decodeString(CURRENT_LANGUAGE_TAG, Locale.getDefault().toLanguageTag())


    private fun <T : BaseSysPreset> getHasEventSysPresetNewVersionKey(clazz: Class<T>) = when (clazz) {
        DietSysPresetEntity::class.java -> FLAG_NEW_VERSION_FOOD_SYS_PRESET
        SportSysPresetEntity::class.java -> FLAG_NEW_VERSION_EXERCISE_SYS_PRESET
        MedicineSysPresetEntity::class.java -> FLAG_NEW_VERSION_MEDICINE_SYS_PRESET
        InsulinSysPresetEntity::class.java -> FLAG_NEW_VERSION_INSULIN_SYS_PRESET
        else -> null
    }

    private fun <T : BaseSysPreset> getEventSysPresetVersionKey(clazz: Class<T>) = when (clazz) {
        DietSysPresetEntity::class.java -> VERSION_FOOD_SYS_PRESET
        SportSysPresetEntity::class.java -> VERSION_EXERCISE_SYS_PRESET
        MedicineSysPresetEntity::class.java -> VERSION_MEDICINE_SYS_PRESET
        InsulinSysPresetEntity::class.java -> VERSION_INSULIN_SYS_PRESET
        else -> null
    }

    fun <T : BaseSysPreset> getEventSysPresetVersion(clazz: Class<T>): String {
        return getEventSysPresetVersionKey(clazz)?.let { MmkvUtil.decodeString(it, "") } ?: ""
    }

    fun <T : BaseSysPreset> setEventSysPresetVersion(version: String, clazz: Class<T>) {
        getEventSysPresetVersionKey(clazz)?.let { MmkvUtil.encodeString(it, version) }
    }

    fun <T : BaseSysPreset> getEventSysPresetNewVersion(clazz: Class<T>): String {
        return getHasEventSysPresetNewVersionKey(clazz)?.let { MmkvUtil.decodeString(it, "") } ?: ""
    }

    fun <T : BaseSysPreset> setEventSysPresetNewVersion(newVersion: String, clazz: Class<T>) {
        getHasEventSysPresetNewVersionKey(clazz)?.let { MmkvUtil.encodeString(it, newVersion) }
    }


    fun setLanguageVersion(version: String) = MmkvUtil.encodeString(VERSION_LANGUAGE, version)
    fun getLanguageVersion(): String = MmkvUtil.decodeString(VERSION_LANGUAGE, "")
    fun setLanguageNewVersion(version: String) {
        MmkvUtil.encodeString(FLAG_NEW_VERSION_LANGUAGE, version)
    }

    fun getLanguageNewVersion(): String = MmkvUtil.decodeString(FLAG_NEW_VERSION_LANGUAGE, "")
    fun setUnitVersion(version: String) = MmkvUtil.encodeString(VERSION_UNIT, version)
    fun getUnitVersion(): String = MmkvUtil.decodeString(VERSION_UNIT, "")
    fun setUnitNewVersion(version: String) {
        MmkvUtil.encodeString(FLAG_NEW_VERSION_UNIT, version)
    }

    fun getUnitNewVersion(): String = MmkvUtil.decodeString(FLAG_NEW_VERSION_UNIT, "")

    fun setResourceVersion(version: String) = MmkvUtil.encodeString(RESOURCE_VERSION, version)
    fun getResourceVersion(): String = MmkvUtil.decodeString(RESOURCE_VERSION, "")
    fun setLastLoginEventDownloadState(key: String, isSuccess: Boolean) = MmkvUtil.encodeBoolean(key, isSuccess)
    fun isLastLoginEventDownloadSuccess(key: String): Boolean = MmkvUtil.decodeBoolean(key, true)

    fun setEventSyncTask(key: String, tasks: DataSyncController.SyncTaskItemList?) {
        MmkvUtil.encodeString(key, tasks?.toString() ?: "")
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

    fun saveProfile(profile: String) = MmkvUtil.encodeString(USER_AVATAR, profile)
    fun setLogin(isLogin: Boolean) = MmkvUtil.encodeBoolean(FLAG_LOGIN, isLogin)
    fun isLogin(): Boolean = MmkvUtil.decodeBoolean(FLAG_LOGIN, false)
    fun saveToken(token: String) = MmkvUtil.encodeString(TOKEN, token)
    fun getToken(): String = MmkvUtil.decodeString(TOKEN, "")
    fun saveHypo(value: Float) = MmkvUtil.encodeFloat(HYPO, value)
    fun getHypo() = MmkvUtil.decodeFloat(HYPO, ThresholdManager.DEFAULT_HYPO)

    fun saveHyper(value: Float) = MmkvUtil.encodeFloat(HYPER, value)

    fun getHyper() = MmkvUtil.decodeFloat(HYPER, ThresholdManager.DEFAULT_HYPER)

    fun saveAppLaunched() = MmkvUtil.encodeBoolean(IS_APP_FIRST_LAUNCH, false)
    fun isAppFirstLaunch() = MmkvUtil.decodeBoolean(IS_APP_FIRST_LAUNCH, true)
    fun saveUserId(id: String) = MmkvUtil.encodeString(USER_ID, id)
    fun getUserId():String = MmkvUtil.decodeString(USER_ID,"")
    fun getOnlineServiceMsgCount() = MmkvUtil.decodeInt(UserInfoManager.instance().userId(), 0)

    fun setOnlineServiceMsgCount(count: Int) =
        MmkvUtil.encodeInt(UserInfoManager.instance().userId(), count)

    fun getEnquireFlag(flag: String): Boolean = MmkvUtil.decodeBoolean(flag, false)

    fun saveEnquireFlag(flag: String, value: Boolean) = MmkvUtil.encodeBoolean(flag, value)

    fun getTheme(): Int = MmkvUtil.decodeInt(THEME, 0)

    fun saveTheme(index: Int) = MmkvUtil.encodeInt(THEME, index)

    fun getGlucoseUnit() = MmkvUtil.decodeInt(GLUCOSE_UNIT, 0)

    fun saveGlucoseUnit(index: Int) = MmkvUtil.encodeInt(GLUCOSE_UNIT, index)

    fun saveCustomerIconPosition(left: Int, top: Int, right: Int, bottom: Int) {
        saveCustomerServiceIconLeft(left)
        saveCustomerServiceIconTop(top)
        saveCustomerServiceIconRight(right)
        saveCustomerServiceIconBottom(bottom)
    }

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
        MmkvUtil.encodeLong("$APP_CHECK_UPDATE_DATE-${BuildConfig.VERSION_NAME}", date)

    fun getAppCheckVersionTime(): Long =
        MmkvUtil.decodeLong("$APP_CHECK_UPDATE_DATE-${BuildConfig.VERSION_NAME}", 0)

    fun getSettings() = MmkvUtil.decodeParcelable(SETTINGS, SettingsEntity::class.java)
    fun saveSettings(parcelable: Parcelable) = MmkvUtil.encodeParcelable(SETTINGS, parcelable)
}