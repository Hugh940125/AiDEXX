package com.microtech.aidexx.ble.device.model

import com.microtech.aidexx.ble.device.entity.CalibrationInfo
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.CalibrateEntity_
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.TimeUtils
import com.microtechmd.blecomm.controller.BleController
import com.microtechmd.blecomm.controller.BleControllerProxy
import com.microtechmd.blecomm.parser.AidexXHistoryEntity
import java.util.Date

/**
 *@date 2023/3/6
 *@author Hugh
 *@desc
 */

const val X_NAME = "AiDEX X"

abstract class DeviceModel(val entity: TransmitterEntity) {
    var faultType = 0 // 1.异常状态，可恢复 2.需要更换
    var targetEventIndex = 0
    var nextEventIndex = 0
    var nextFullEventIndex = 0
    var nextCalIndex = 0
    var latestAdTime = 0L
    var latestAd: Any? = null
    var glucose: Float? = null
    var lastHistoryTime: Date? = null
    var controller: BleController? = null
    var isHistoryValid: Boolean = false
    var isMalfunction: Boolean = false
    var glucoseLevel: GlucoseLevel? = null
    var glucoseTrend: GlucoseTrend? = null
    var latestHistory: AidexXHistoryEntity? = null
    var isGettingTransmitterData = false
    var alert: ((time: String, type: Int) -> Unit)? = null
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

    fun deviceType(): Int {
        return entity.deviceType
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

    abstract suspend fun uploadPairInfo()
    abstract fun savePair()

    abstract suspend fun deletePair()

    fun updateStart(sensorStartTime: Date) {
        val sensorId = EncryptUtils.md5(
            UserInfoManager.instance().userId()
                    + entity.deviceSn
                    + entity.startTimeToIndex(sensorStartTime)
                    + entity.startTimeToIndex(sensorStartTime)
        )
        sensorId.let {
            val lastBrief = ObjectBox.cgmHistoryBox!!.query()
                .equal(RealCgmHistoryEntity_.sensorId, it)
                .orderDesc(RealCgmHistoryEntity_.timeOffset)
                .build()
                .findFirst()
            entity.eventIndex = lastBrief?.timeOffset ?: 0
            nextEventIndex = entity.eventIndex + 1
            val lastRaw = ObjectBox.cgmHistoryBox!!.query()
                .equal(RealCgmHistoryEntity_.sensorId, it)
                .notNull(RealCgmHistoryEntity_.rawIsValid)
                .orderDesc(RealCgmHistoryEntity_.timeOffset)
                .build()
                .findFirst()
            entity.fullEventIndex = lastRaw?.timeOffset ?: 0
            nextFullEventIndex = entity.fullEventIndex + 1
            val lastCal = ObjectBox.calibrationBox!!.query()
                .equal(CalibrateEntity_.sensorId, it)
                .orderDesc(CalibrateEntity_.index)
                .build()
                .findFirst()
            entity.calIndex = lastCal?.index ?: 0
            nextCalIndex = entity.calIndex + 1
        }
        entity.sensorStartTime = sensorStartTime
        LogUtil.eAiDEX("Init data index, Start time:${sensorStartTime.date2ymdhm()}, Brief index:${entity.eventIndex}, Raw index:${entity.fullEventIndex},cal index:${entity.calIndex}")
    }

    fun reset() {
        entity.sensorStartTime = null
    }
}