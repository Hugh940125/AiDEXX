package com.microtech.aidexx.db.entity

import com.microtech.aidexx.utils.ThresholdManager
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.TimeZone

@Entity
class SettingsEntity {
    @Id
    var idx: Long? = null
    var language: String? = "zh-CN"
    var timeZone: String = TimeZone.getDefault().id
    var unit = 0
    var theme = 1
    var userSettingId: String? = null
    var authorizationId: String? = null
    var alertType: Int = 3
    var alertRate: Int = 30
    var lowAlertSwitch = 0
    var lowLimitMg = ThresholdManager.DEFAULT_HYPO
    var highAlertSwitch = 0
    var highLimitMg = ThresholdManager.DEFAULT_HYPER
    var fastUpSwitch = true
    var isFastDownEnable = 0
    var fastDownSwitch = 0
    var urgentLowAlertSwitch = 0
    var urgentLowMg = ThresholdManager.URGENT_HYPO
    var urgentAlertType = 3
    var urgentAlertRate: Int = 5
    var signalMissingSwitch = 0
    var signalMissingAlertType = 3
    var signalMissingAlertRate = 15
    var needSync = false


    constructor(authorizationId: String?) {
        this.authorizationId = authorizationId
    }

    constructor()

    override fun toString(): String {
        return "AlertSettingsEntity(idx=$idx, alertMethod=$alertType, alertFrequency=$alertRate, isHypoEnable=$lowAlertSwitch, hypoThreshold=$lowLimitMg, isHyperEnable=$highAlertSwitch, hyperThreshold=$highLimitMg, isFastUpEnable=$fastUpSwitch, isFastDownEnable=$isFastDownEnable, isUrgentLowEnable=$fastDownSwitch, urgentAlertMethod=$urgentAlertType, urgentAlertFrequency=$urgentAlertRate, isSignalLossEnable=$signalMissingSwitch, signalLossMethod=$signalMissingAlertType, signalLossFrequency=$signalMissingAlertRate)"
    }
}