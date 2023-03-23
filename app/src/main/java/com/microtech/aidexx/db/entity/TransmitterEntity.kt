package com.microtech.aidexx.db.entity

import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.utils.ThresholdManager
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import java.util.*


@Entity
class TransmitterEntity {
    @Id
    var idx: Long? = null
    var id: String? = null

    @Unique
    var deviceMac: String? = null
    var version: String? = null
    var deviceSn: String? = null
    var accessId: ByteArray? = null
    var encryptionKey: ByteArray? = null
    var sensorStartTime: Date? = null //开始时间
    var needReplace = false
    var deviceModel: Int = 0
    var expirationTime: Int = 15
        set(value) {
            field = value
            et = value
        }
    var sensorIndex: Int = 0
    var eventIndex: Int = 0
    var fullEventIndex: Int = 0
    var fullSensorIndex: Int = 0
    var hyperThreshold: Float = ThresholdManager.DEFAULT_HYPER
    var hypoThreshold: Float = ThresholdManager.DEFAULT_HYPO
    var deviceKey: String? = null
    var et: Int = 0
    var deviceName: String? = null
    var deviceType: Int = 2

    constructor()

    constructor(sn: String) {
        deviceSn = sn
    }

    fun startTimeToIndex(): Int {
        return sensorStartTime?.time?.millisToMinutes()!!
    }

    override fun toString(): String {
        return "TransmitterEntity(et=$expirationTime,idx=$idx, id=$id, deviceMac='$deviceMac', deviceSn='$deviceSn', accessId=${accessId?.contentToString()}, encryptionKey=${encryptionKey?.contentToString()}, sensorIndex=$sensorIndex, eventIndex=$eventIndex, fullEventIndex=$fullEventIndex, fullSensorIndex=$fullSensorIndex, hyperThreshold=$hyperThreshold, hypoThreshold=$hypoThreshold)"
    }
}