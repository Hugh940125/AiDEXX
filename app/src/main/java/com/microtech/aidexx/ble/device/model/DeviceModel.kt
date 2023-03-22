package com.microtech.aidexx.ble.device.model

import com.microtech.aidexx.ble.device.entity.CalibrationInfo
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.utils.TimeUtils
import com.microtechmd.blecomm.controller.BleController
import com.microtechmd.blecomm.controller.BleControllerProxy
import com.microtechmd.blecomm.entity.BleMessage
import com.microtechmd.blecomm.parser.AidexXHistoryEntity
import java.util.*

/**
 *@date 2023/3/6
 *@author Hugh
 *@desc
 */
abstract class DeviceModel(val entity: TransmitterEntity) {
    var faultType = 0 // 1.异常状态，可恢复 2.需要更换
    var targetEventIndex = 0
    var nextEventIndex = 0
    var nextFullEventIndex = 0
    var latestAdTime = 0L
    var latestAd: Any? = null
    var glucose: Float? = null
    var lastHistoryTime: Date? = null
    lateinit var controller: BleController
    var isHistoryValid: Boolean = false
    var isMalfunction: Boolean = false
    var sensorStartTime: Date? = null
    var glucoseLevel: GlucoseLevel? = null
    var glucoseTrend: GlucoseTrend? = null
    var latestHistory: AidexXHistoryEntity? = null
    var messageCallBack: ((msg: BleMessage) -> Unit)? = null
    var onCalibrationCallback: ((allow: Boolean) -> Unit)? = null
    var onCalibrationPermitChange: ((allow: Boolean) -> Unit)? = null
    var minutesAgo: Int? = null
        private set
        get() {
            field = if (lastHistoryTime == null) {
                null
            } else {
                (TimeUtils.currentTimeMillis - lastHistoryTime!!.time).millisToMinutes()
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

    abstract fun getController(): BleControllerProxy

    abstract fun saveBriefHistoryFromConnect(data: ByteArray)

    abstract fun saveRawHistoryFromConnect(data: ByteArray)

    abstract fun isDataValid(): Boolean

    abstract fun getSensorRemainingTime(): Int?

    abstract fun calibration(info: CalibrationInfo)

    abstract fun isAllowCalibration(): Boolean

    abstract suspend fun savePair(
        model: Int = 0,
        version: String? = null,
        success: (() -> Unit)?,
        fail: (() -> Unit)?
    )

    fun disconnect() {
        controller.disconnect()
    }

    fun updateStartTime(sensorStartTime: Date?, callback: ((Boolean) -> Unit)? = null) {
        ObjectBox.runAsync({
            entity.sensorStartTime = sensorStartTime
            ObjectBox.transmitterBox!!.put(entity)
        }, onSuccess = {
            callback?.invoke(true)
        }, onError = {
            callback?.invoke(false)
        })
    }

    fun setCalibrationResult(boolean: Boolean) {
        onCalibrationCallback?.invoke(boolean)
    }
}