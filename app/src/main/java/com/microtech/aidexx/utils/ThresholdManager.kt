package com.microtech.aidexx.utils

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.db.entity.AlertSettingsEntity
import com.microtech.aidexx.ui.setting.alert.AlertUtil
import kotlinx.coroutines.launch

object ThresholdManager {

    const val GLUCOSE_LOW_LIMIT = 2f
    const val GLUCOSE_UP_LIMIT = 25f
    const val URGENT_HYPO = 3f
    const val DEFAULT_HYPO = 3.9f
    const val DEFAULT_HYPER = 10f

    const val SUPER_FAST_DOWN = -0.17
    const val FAST_DOWN = -0.11
    const val SLOW_DOWN = -0.06
    const val SLOW_UP = 0.06
    const val FAST_UP = 0.11
    const val SUPER_FAST_UP = 0.17

    lateinit var alertSetting: AlertSettingsEntity

    init {
        AidexxApp.mainScope.launch {
            alertSetting = AlertUtil.getAlertSettings()
        }
    }

    var hyper: Float = 0f
        set(value) {
            AlertUtil.setHyperThreshold(value)
        }
        get() {
            if (this::alertSetting.isInitialized) {
                field = alertSetting.hyperThreshold
            }
            return field
        }

    var hypo: Float = 0f
        set(value) {
            AlertUtil.setHypoThreshold(value)
        }
        get() {
            if (this::alertSetting.isInitialized) {
                field = alertSetting.hypoThreshold
            }
            return field
        }
}