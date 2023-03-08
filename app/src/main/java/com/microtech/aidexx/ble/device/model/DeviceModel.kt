package com.microtech.aidexx.ble.device.model

import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.utils.TimeUtils
import com.microtechmd.blecomm.controller.BleController
import com.microtechmd.blecomm.entity.BleMessage
import java.util.*

/**
 *@date 2023/3/6
 *@author Hugh
 *@desc
 */
abstract class DeviceModel(val entity: TransmitterEntity) {
    open lateinit var mController: BleController
    var messageCallBack: ((msg: BleMessage) -> Unit)? = null
    var isHistoryValid: Boolean = false
    var isMalfunction: Boolean = false
    var sensorStartTime: Date? = null
    var targetSensorIndex = 0
    var targetEventIndex = 0
    var nextEventIndex = 0
    var nextFullEventIndex = 0
    var latestAdTime = 0L
    var glucoseLevel: GlucoseLevel? = null
    var glucoseTrend: GlucoseTrend? = null
    var lastHistoryTime: Date? = null
    var minutesAgo: Int? = null
        private set
        get() {
            if (lastHistoryTime == null) {
                field = null
            } else {
                field = (TimeUtils.currentTimeMillis - lastHistoryTime!!.time).millisToMinutes()
            }
            return field
        }

    enum class GlucoseLevel { LOW, NORMAL, HIGH }
    enum class GlucoseTrend { SUPER_FAST_DOWN, FAST_DOWN, DOWN, STEADY, UP, FAST_UP, SUPER_FAST_UP }

    fun deviceId(): String? {
        return entity.id
    }

    fun isPaired(): Boolean {
        return entity.accessId != null
    }

    abstract fun handleAdvertisement(data: ByteArray)

    abstract fun getController():BleController

    abstract fun saveBriefHistoryFromConnect(data: ByteArray)

    abstract fun saveRawHistoryFromConnect(data: ByteArray)

    fun disconnect() {
        mController.disconnect()
    }
}