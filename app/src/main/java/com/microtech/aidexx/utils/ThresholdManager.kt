package com.microtech.aidexx.utils

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.db.entity.AlertSettingsEntity
import com.microtech.aidexx.ui.setting.alert.AlertUtil
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import kotlinx.coroutines.launch

object ThresholdManager {

    const val GLUCOSE_LOW_LIMIT = 2f
    const val GLUCOSE_UP_LIMIT = 25f
    const val URGENT_HYPO = 3f
    const val DEFAULT_HYPO = 70.2f
    const val DEFAULT_HYPER = 180f

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
            if (alertSetting.hyperThreshold != DEFAULT_HYPER || alertSetting.hypoThreshold != DEFAULT_HYPO) {
                EventBusManager.send(EventBusKey.EVENT_HYP_CHANGE, true)
            }
        }
    }

    var hyper: Float = DEFAULT_HYPER
        set(value) {
            if (value == field) {
                return
            }
            alertSetting.hyperThreshold = value
            AlertUtil.setHyperThreshold(value)
        }
        get() {
            if (this::alertSetting.isInitialized) {
                field = alertSetting.hyperThreshold
            }
            return field
        }

    var hypo: Float = DEFAULT_HYPO
        set(value) {
            if (value == field) {
                return
            }
            alertSetting.hypoThreshold = value
            AlertUtil.setHypoThreshold(value)
        }
        get() {
            if (this::alertSetting.isInitialized) {
                field = alertSetting.hypoThreshold
            }
            return field
        }
}