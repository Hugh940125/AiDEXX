package com.microtech.aidexx.utils

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.db.entity.SettingsEntity
import com.microtech.aidexx.ui.setting.SettingsManager
import com.microtech.aidexx.ui.setting.alert.AlertUtil
import kotlinx.coroutines.launch

object ThresholdManager {

    const val GLUCOSE_LOW_LIMIT = 2f * 18f
    const val GLUCOSE_UP_LIMIT = 25f * 18f
    const val URGENT_HYPO = 3f * 18f
    const val DEFAULT_HYPO = 3.9f * 18f
    const val DEFAULT_HYPER = 10f * 18f

    const val SUPER_FAST_DOWN = -0.17
    const val FAST_DOWN = -0.11
    const val SLOW_DOWN = -0.06
    const val SLOW_UP = 0.06
    const val FAST_UP = 0.11
    const val SUPER_FAST_UP = 0.17

    lateinit var alertSetting: SettingsEntity

    init {
        AidexxApp.mainScope.launch {
            alertSetting = SettingsManager.settingEntity!!
        }
    }

    var hyper: Float = DEFAULT_HYPER
        set(value) {
            if (value == field) {
                return
            }
            alertSetting.highLimitMg = value
            AlertUtil.setHyperThreshold(value)
        }
        get() {
            if (this::alertSetting.isInitialized) {
                field = alertSetting.highLimitMg
            }
            return field
        }

    var hypo: Float = DEFAULT_HYPO
        set(value) {
            if (value == field) {
                return
            }
            alertSetting.lowLimitMg = value
            AlertUtil.setHypoThreshold(value)
        }
        get() {
            if (this::alertSetting.isInitialized) {
                field = alertSetting.lowLimitMg
            }
            return field
        }
}