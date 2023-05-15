package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.utils.toGlucoseValue
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.parser.CgmHistoryEntity
import io.objectbox.annotation.ConflictStrategy
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import io.objectbox.annotation.Unique
import java.util.Date

@Entity
class RealCgmHistoryEntity : EventEntity, CgmHistoryEntity {
    @Id(assignable = true)
    override var idx: Long? = null
    var briefUploadState = 0 //0原始转态 1更新待上传 2已上传
    var rawUploadState = 0
    var calUploadState = 0

    @Index
    override var state: Int = 0
    override var id: String? = null

    @Index(type = IndexType.HASH)
    var deviceSn: String? = null
    var cgmRecordId: String? = null

    @Index(type = IndexType.HASH)
    var sensorId: String? = null

    @Index(type = IndexType.HASH)
    override var userId: String? = null

    @Index
    var deviceTime = Date()

    @Index
    var eventIndex: Int = 0

    @Index
    var sensorIndex: Int = 0

    @Index
    var dataStatus = 0 // 0，原始数据，1代表待上传 2代表已上传

    @Index
    override var recordIndex: Long? = null

    @Index
    override var deleteStatus: Int = 0
    var eventType: Int? = null
    var glucose: Float? = null
    var eventDataOrigin: Float? = null
    var cf: Float = 1f //校准系数
    var offset: Float = 0f //校准偏移量
    var calibrationIsValid: Int = 0
    var index: Int = 0
    var rawIsValid: Int = 0
    var glucoseIsValid: Int = 0
    var quality: Int = 0
    var status: Int = 0
    var autoIncrementColumn: Long = 0
    var timeOffset: Int = 0
    var rawOne: Float? = null
    var rawTwo: Float? = null
    var rawVc: Float? = null

    @Index
    var eventWarning: Int = 0  //0默认 1高血糖 2低血糖
    var referenceGlucose: Float = 0f // 校准值

    @Index(type = IndexType.HASH)
    var deviceId: String? = null
    var type = 0; // type为0正常数据，1代表占位数据

    @Transient
    override var createTime: Date = Date()

    @Transient
    var updateTime: Date = Date()

    @Transient
    override var language: String = ""
    override var uploadState: Int = 0
    override var recordId: String? = null
    @Index(type = IndexType.HASH)
    @Unique(onConflict = ConflictStrategy.REPLACE)
    var frontRecordId: String? = null
    fun updateRecordUUID(): String {
        val userID = UserInfoManager.instance().userId();
        val deviceId = TransmitterManager.instance().getDefault()?.deviceId()
        val uuidStr = StringBuffer()
        uuidStr.append(userID)
            .append(deviceId)
            .append(deviceTime.time / 1000)
            .append(sensorIndex)
            .append(eventIndex)
            .append(eventType)
        return EncryptUtils.md5(uuidStr.toString())
    }

    override fun _setDatetime(datetime: Long) {
        this.deviceTime = Date(datetime * 1000)
    }

    override fun _setEventIndex(eventIndex: Int) {
        this.eventIndex = eventIndex
    }

    override fun _setSensorIndex(sensorIndex: Int) {
        this.sensorIndex = sensorIndex
    }

    override fun _setEventType(eventType: Int) {
        this.eventType = eventType
    }

    override fun _setEventValue(eventValue: Float) {
        this.glucose = eventValue
    }

    var rawData1: Float? = null
    var rawData2: Float? = null
    var rawData3: Float? = null
    var rawData4: Float? = null
    var rawData5: Float? = null
    var rawData6: Float? = null
    var rawData7: Float? = null
    var rawData8: Float? = null
    var rawData9: Float? = null

    override fun _setRawValue(rawValue: FloatArray) {
        rawData1 = rawValue[0]
        rawData2 = rawValue[1]
        rawData3 = rawValue[2]
        rawData4 = rawValue[3]
        rawData5 = rawValue[4]
        rawData6 = rawValue[5]
        rawData7 = rawValue[6]
        rawData8 = rawValue[7]
        rawData9 = rawValue[8]

    }


    @Transient
    override var time: Date = deviceTime
        get() {
            return deviceTime
        }
        set(time) {
            field = time
            deviceTime = time
        }

    override fun getEventDescription(res: Resources): String {
        return when (eventWarning) {
            History.HISTORY_LOCAL_HYPER -> res.getString(R.string.high_gluecose_alert)
            History.HISTORY_LOCAL_HYPO -> res.getString(R.string.low_gluecose_alert)
            History.HISTORY_BLOOD_GLUCOSE -> res.getString(R.string.title_bg)
            History.HISTORY_LOCAL_URGENT_HYPO -> res.getString(R.string.Urgent_Low_Alarm)
            0, null -> when (eventType) {
                History.HISTORY_HYPER -> res.getString(R.string.high_gluecose_alert)
                History.HISTORY_HYPO -> res.getString(
                    R.string.low_gluecose_alert
                )
                else -> ""
            }
            else -> ""
        }
    }


    override fun getValueDescription(res: Resources): String {
        return when (eventWarning) {
            History.HISTORY_LOCAL_HYPER -> res.getString(
                R.string.hyper_description,
                glucose?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            History.HISTORY_LOCAL_HYPO -> res.getString(
                R.string.hypo_description,
                glucose?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            History.HISTORY_BLOOD_GLUCOSE -> res.getString(
                R.string.bg_description,
                glucose?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            0 -> when (eventType) {
                History.HISTORY_HYPER -> res.getString(
                    R.string.hyper_description,
                    glucose?.toGlucoseValue()
                ) + UnitManager.glucoseUnit.text
                History.HISTORY_HYPO -> res.getString(
                    R.string.hypo_description,
                    glucose?.toGlucoseValue()
                ) + UnitManager.glucoseUnit.text
                else -> ""
            }
            else -> ""
        }
    }

    fun isHighOrLow(): Boolean {
        glucose?.let {
            if (it > ThresholdManager.hyper) {
                return true
            } else if (it < ThresholdManager.hypo) {
                return true
            }
        }
        return false
    }

    fun isHighOrLowGlucose(): Boolean {
        val model = TransmitterManager.instance().getDefault()
        if (model != null) {
            if (glucose!! > ThresholdManager.hyper) {
                return true
            } else if (glucose!! < ThresholdManager.hypo) {
                return true
            }
        }
        return false
    }


    fun getHighOrLowGlucoseType(): Int {
        if (glucose!! > ThresholdManager.hyper * 18) {
            return 2
        } else if (glucose!! < ThresholdManager.hypo * 18 && glucose!! >= ThresholdManager.URGENT_HYPO * 18) {
            return 1
        } else if (glucose!! < ThresholdManager.URGENT_HYPO * 18) {
            return 3
        }
        return 0
    }


    fun updateEventWarning() {
        eventWarning = History.HISTORY_LOCAL_NORMAL
        if (glucose!! > ThresholdManager.hyper * 18) {
            eventWarning = History.HISTORY_LOCAL_HYPER
        } else if (glucose!! < ThresholdManager.hypo * 18 && glucose!! >= ThresholdManager.URGENT_HYPO * 18) {
            eventWarning = History.HISTORY_LOCAL_HYPO
        } else if (glucose!! < ThresholdManager.URGENT_HYPO * 18) {
            eventWarning = History.HISTORY_LOCAL_URGENT_HYPO
        }
    }

    override fun toString(): String {
        return "CgmHistoryEntity(eventWarning=$eventWarning, idx=$idx, state=$state, id=$id, deviceSn=$deviceSn, deviceTime=$deviceTime, eventIndex=$eventIndex, sensorIndex=$sensorIndex, dataStatus=$dataStatus, recordIndex=$recordIndex, deleteStatus=$deleteStatus, eventType=$eventType, glucose=$glucose, deviceId=$deviceId, type=$type, authorizationId=${this.userId}, frontRecordId=${this.frontRecordId}, rawData1=$rawData1, rawData2=$rawData2, rawData3=$rawData3, rawData4=$rawData4, rawData5=$rawData5, rawData6=$rawData6, rawData7=$rawData7, rawData8=$rawData8, rawData9=$rawData9)"
    }

    fun isGlucoseIsValid() = glucoseIsValid == 1 && status == History.STATUS_OK

    fun isCalibrationIsValid() = calibrationIsValid == 1

}