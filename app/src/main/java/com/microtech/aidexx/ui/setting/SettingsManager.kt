package com.microtech.aidexx.ui.setting

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.minutesToMillis
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.SettingsEntity
import com.microtech.aidexx.db.entity.SettingsEntity_
import com.microtech.aidexx.ui.setting.alert.AlertUtil
import io.objectbox.kotlin.awaitCallInTx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *@date 2023/6/26
 *@author Hugh
 *@desc
 */
object SettingsManager {

    private lateinit var settingEntity: SettingsEntity

    suspend fun loadSettingsFromDb(): SettingsEntity? {
        return ObjectBox.store.awaitCallInTx {
            val findFirst = ObjectBox.AlertSettingsBox!!.query()
                .equal(
                    SettingsEntity_.authorizationId,
                    UserInfoManager.instance().userId()
                )
                .orderDesc(SettingsEntity_.idx)
                .build()
                .findFirst()
            findFirst?.let {
                AlertUtil.alertFrequency = it.alertRate.minutesToMillis()
                AlertUtil.urgentFrequency = it.urgentAlertRate.minutesToMillis()
                AlertUtil.hyperSwitchEnable = it.highAlertSwitch == 0
                AlertUtil.hypoSwitchEnable = it.lowAlertSwitch == 0
                AlertUtil.urgentLowSwitchEnable = it.urgentLowAlertSwitch == 0
            }
            findFirst
        }
    }

    suspend fun getSettings(): SettingsEntity {
        if (!::settingEntity.isInitialized) {
            settingEntity = loadSettingsFromDb() ?: SettingsEntity(UserInfoManager.instance().userId())
        }
        return settingEntity
    }

    fun saveSetting(settings: SettingsEntity) {
        settings.needSync = true
        ObjectBox.AlertSettingsBox!!.put(settings)
    }

    fun setLanguage(lang: String) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.language = lang
        saveSetting(alertSettings)
    }

    fun setUnit(unit: Int) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.unit = unit
        saveSetting(alertSettings)
    }

    fun setTheme(theme: Int) = AidexxApp.mainScope.launch(Dispatchers.IO) {
        val alertSettings = getSettings()
        alertSettings.theme = theme
        saveSetting(alertSettings)
    }
}