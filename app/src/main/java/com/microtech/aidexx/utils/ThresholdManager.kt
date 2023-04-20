package com.microtech.aidexx.utils

import com.microtech.aidexx.ui.setting.alert.AlertUtil

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

    var hyper: Float = 0f
        set(value) {
            AlertUtil.setHyperThreshold(value)
        }
        get() {
            field = AlertUtil.getAlertSettings().hyperThreshold
            return field
        }
    var hypo: Float = 0f
        set(value) {
            AlertUtil.setHypoThreshold(value)
        }
        get() {
            field = AlertUtil.getAlertSettings().hypoThreshold
            return field
        }
}