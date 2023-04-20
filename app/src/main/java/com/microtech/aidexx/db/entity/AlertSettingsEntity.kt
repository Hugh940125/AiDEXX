package com.microtech.aidexx.db.entity

import com.microtech.aidexx.utils.ThresholdManager
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class AlertSettingsEntity {
    @Id
    var idx: Long? = null
    var authorizationId: String? = null
    var alertMethod: Int = 2
    var alertFrequency: Int = 2
    var isHypoEnable = true
    var hypoThreshold = ThresholdManager.DEFAULT_HYPO
    var isHyperEnable = true
    var hyperThreshold = ThresholdManager.DEFAULT_HYPER
    var isFastUpEnable = true
    var isFastDownEnable = true
    var isUrgentLowEnable = true
    var urgentAlertMethod = 2
    var urgentAlertFrequency: Int = 0
    var isSignalLossEnable = true
    var signalLossMethod = 2
    var signalLossFrequency = 0
    var needSync = false


    constructor(authorizationId: String?) {
        this.authorizationId = authorizationId
    }

    constructor()

    override fun toString(): String {
        return "AlertSettingsEntity(idx=$idx, alertMethod=$alertMethod, alertFrequency=$alertFrequency, isHypoEnable=$isHypoEnable, hypoThreshold=$hypoThreshold, isHyperEnable=$isHyperEnable, hyperThreshold=$hyperThreshold, isFastUpEnable=$isFastUpEnable, isFastDownEnable=$isFastDownEnable, isUrgentLowEnable=$isUrgentLowEnable, urgentAlertMethod=$urgentAlertMethod, urgentAlertFrequency=$urgentAlertFrequency, isSignalLossEnable=$isSignalLossEnable, signalLossMethod=$signalLossMethod, signalLossFrequency=$signalLossFrequency)"
    }
}