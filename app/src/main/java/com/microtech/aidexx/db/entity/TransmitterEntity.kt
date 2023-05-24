package com.microtech.aidexx.db.entity

import com.microtech.aidexx.common.millisToIntSeconds
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.ThresholdManager
import io.objectbox.annotation.*
import java.util.*


const val TYPE_G7 = 1
const val TYPE_X = 2

@Entity
class TransmitterEntity {
    var calIndex: Int = 0

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
        set(value) {
            field = value
            if (value != null) {
                val index = startTimeToIndex()
                sensorIndex = index
                sensorId = EncryptUtils.md5(UserInfoManager.instance().userId() + deviceSn + index + sensorIndex)
            }
        }
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
    var deviceType: Int = TYPE_X

    @Index(type = IndexType.HASH)
    var sensorId: String? = null

    constructor()

    constructor(sn: String) {
        deviceSn = sn
    }

    fun startTimeToIndex(): Int {
        if (sensorStartTime == null) return 0
        return sensorStartTime?.time?.millisToIntSeconds() ?: 0
    }

    fun startTimeToIndex(startTime: Date?): Int {
        if (startTime == null) return 0
        return startTime.time.millisToIntSeconds()
    }

    override fun toString(): String {
        return "TransmitterEntity(et=$expirationTime,idx=$idx, id=$id, deviceMac='$deviceMac', deviceSn='$deviceSn', accessId=${accessId?.contentToString()}, encryptionKey=${encryptionKey?.contentToString()}, sensorIndex=$sensorIndex, eventIndex=$eventIndex, fullEventIndex=$fullEventIndex, fullSensorIndex=$fullSensorIndex, hyperThreshold=$hyperThreshold, hypoThreshold=$hypoThreshold)"
    }
}