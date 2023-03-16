package com.microtech.aidexx.ble.device.entity

import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtechmd.blecomm.constant.Glucose
import com.microtechmd.blecomm.parser.CgmBroadcastEntity
import com.microtechmd.blecomm.parser.CgmHistoryEntity

class BroadcastEntity : CgmBroadcastEntity {
    var receivedTime = 0L //记录接收的时间
    var datetime = 0L
        private set
    override fun _setDatetime(datetime: Long) {
        this.datetime = datetime
    }

    var battery = 0
        private set
    override fun _setBattery(battery: Int) {
        this.battery = battery
    }

    var glucose = 0f
        private set
    override fun _setGlucose(glucose: Float) {
        this.glucose = glucose
    }
    var primary = 0
        private set
    override fun _setPrimary(primary: Int) {
        this.primary = primary
    }

    var state: Int = Glucose.STATE_OK
        private set

    override fun _setState(state: Int) {
        this.state = state
    }

    var cgmHistory: CgmHistoryEntity? = null
        private set
    override fun _setHistory(cgmHistory: CgmHistoryEntity) {
        this.cgmHistory = cgmHistory as RealCgmHistoryEntity
    }
}