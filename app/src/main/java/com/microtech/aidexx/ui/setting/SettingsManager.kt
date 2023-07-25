package com.microtech.aidexx.ui.setting

import com.google.gson.Gson
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.entity.SettingsEntity
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


/**
 *@date 2023/6/26
 *@author Hugh
 *@desc
 */
object SettingsManager {

    var settingEntity: SettingsEntity? = null
        get() {
            if (field == null) {
                field = MmkvManager.getSettings() ?: SettingsEntity()
            }
            return field
        }

    fun getHyperAlertSwitch(): Boolean {
        return settingEntity?.highAlertSwitch == 0
    }

    fun getHypoAlertSwitch(): Boolean {
        return settingEntity?.lowAlertSwitch == 0
    }

    fun getUrgentAlertSwitch(): Boolean {
        return settingEntity?.urgentLowAlertSwitch == 0
    }

    fun saveSetting(settings: SettingsEntity?) {
        settings?.let {
            it.version++
            MmkvManager.saveSettings(it)
        }
    }

    fun setLanguage(lang: String) {
        settingEntity?.language = lang
        saveSetting(settingEntity)
    }

    fun setUnit(unit: Int) {
        settingEntity?.unit = unit
        saveSetting(settingEntity)
    }

    fun setTheme(theme: Int) {
        settingEntity?.theme = theme
        saveSetting(settingEntity)
    }

    suspend fun uploadSettings() {
        settingEntity?.let {
            if (it.version > 0 && it.userSettingId != null) {
                val toJson = Gson().toJson(it)
                val toRequestBody = toJson.toRequestBody("application/json".toMediaType())
                when (val response = ApiService.instance.uploadSetting(toRequestBody)) {
                    is ApiResult.Success -> {
                        if (it.version == settingEntity!!.version) {
                            it.version = 0
                            MmkvManager.saveSettings(it)
                            LogUtil.eAiDEX("Settings upload success:$it")
                        }
                    }

                    is ApiResult.Failure -> {
                        LogUtil.eAiDEX("Settings upload fail:${response.msg}")
                    }
                }
            }
        }
    }

    suspend fun downloadSettings(userId: String) {
        if (UserInfoManager.instance().userId() != userId) {
            return
        }
        when (val response = ApiService.instance.downloadSetting(userId)) {
            is ApiResult.Success -> {
                response.result.data?.let { settingsEntity ->
                    settingEntity = settingsEntity
                    MmkvManager.saveSettings(settingsEntity)
                }
            }

            is ApiResult.Failure -> {
                LogUtil.eAiDEX("Settings upload fail:${response.msg}")
            }

        }
    }
}