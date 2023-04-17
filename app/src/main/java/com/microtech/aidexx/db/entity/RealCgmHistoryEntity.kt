package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.toGlucoseValue
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.LogUtils
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.parser.CgmHistoryEntity
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import java.util.*

@Entity
class RealCgmHistoryEntity : EventEntity, CgmHistoryEntity {

    @Id(assignable = true)
    override var idx: Long? = null

    @Index
    override var state: Int = 0
    override var id: String? = null

    @Index
    var deviceSn: String? = null

    @Index
    var deviceTime = Date()
        set(value) {
            field = value
            deviceTimeLong = deviceTime.time / 1000
        }
    var eventIndex = 0
    var sensorIndex = 0

    @Index
    var dataStatus = 0 // 0，原始数据，1代表待上传 2代表已上传

    @Index
    override var recordIndex: Long? = null

    @Index
    override var deleteStatus: Int = 0
    var eventType: Int = History.HISTORY_INVALID
    var eventData: Float? = null
    var eventDataOrgin: Float? = null
    var calFactor: Float = 1f //校准系数
    var calOffset: Float = 0f //校准偏移量
    var eventWarning: Int = 0  //0默认 1高血糖 2低血糖
    var deviceId: String? = null
    var type = 0; // type为0正常数据，1代表占位数据
    var deviceTimeLong: Long? = null

    @Transient
    override var createTime: Date = Date()

    @Transient
    override var language: String = ""

    @Index
    override var authorizationId: String? = null

    override var recordId: String? = null

    @Index
    var recordUuid: String? = null

    fun updateRecordUUID() {
        var userID = UserInfoManager.instance().userId();
        var deviceId = TransmitterManager.instance().getDefault()?.deviceId()
        var uuidStr = StringBuffer();
        uuidStr.append(userID)
            .append(deviceId)
            .append(deviceTime.time / 1000)
            .append(sensorIndex)
            .append(eventIndex)
            .append(eventType)

        LogUtils.data("cgmHistory : userID:" + userID + ", deviceId :" + deviceId + " , deviceSn :" + deviceSn + ", time :" + deviceTime.time / 1000 + " ,eventData :" + eventData + " ,sensorIndex :" + sensorIndex)
        recordUuid = EncryptUtils.md5(uuidStr.toString())
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
        this.eventData = eventValue
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
                eventData?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            History.HISTORY_LOCAL_HYPO -> res.getString(
                R.string.hypo_description,
                eventData?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            History.HISTORY_BLOOD_GLUCOSE -> res.getString(
                R.string.bg_description,
                eventData?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            0 -> when (eventType) {
                History.HISTORY_HYPER -> res.getString(
                    R.string.hyper_description,
                    eventData?.toGlucoseValue()
                ) + UnitManager.glucoseUnit.text
                History.HISTORY_HYPO -> res.getString(
                    R.string.hypo_description,
                    eventData?.toGlucoseValue()
                ) + UnitManager.glucoseUnit.text
                else -> ""
            }
            else -> ""
        }
    }

    fun isHighOrLow(): Boolean {
        eventData?.let {
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
            if (eventData!! > ThresholdManager.hyper) {
                return true
            } else if (eventData!! < ThresholdManager.hypo) {
                return true
            }
        }
        return false
    }


    fun getHighOrLowGlucoseType(): Int {
        val model = TransmitterManager.instance().getDefault()
        if (model != null) {
            if (eventData!! > ThresholdManager.hyper * 18) {
                return 2
            } else if (eventData!! < ThresholdManager.hypo * 18 && eventData!! >= ThresholdManager.URGENT_HYPO * 18) {
                return 1
            } else if (eventData!! < ThresholdManager.URGENT_HYPO * 18) {
                return 3
            }
        }
        return 0
    }


    fun updateEventWarning() {
        val model = TransmitterManager.instance().getDefault()
        eventWarning = History.HISTORY_LOCAL_NORMAL
        if (model != null) {
            if (eventData!! > ThresholdManager.hyper * 18) {
                eventWarning = History.HISTORY_LOCAL_HYPER
            } else if (eventData!! < ThresholdManager.hypo * 18 && eventData!! >= ThresholdManager.URGENT_HYPO * 18) {
                eventWarning = History.HISTORY_LOCAL_HYPO
            } else if (eventData!! < ThresholdManager.URGENT_HYPO * 18) {
                eventWarning = History.HISTORY_LOCAL_URGENT_HYPO
            }
        }
    }

    override fun toString(): String {
        return "CgmHistoryEntity(eventWarning=$eventWarning, idx=$idx, state=$state, id=$id, deviceSn=$deviceSn, deviceTime=$deviceTime, eventIndex=$eventIndex, sensorIndex=$sensorIndex, dataStatus=$dataStatus, recordIndex=$recordIndex, deleteStatus=$deleteStatus, eventType=$eventType, eventData=$eventData, deviceId=$deviceId, type=$type, authorizationId=$authorizationId, recordUuid=$recordUuid, rawData1=$rawData1, rawData2=$rawData2, rawData3=$rawData3, rawData4=$rawData4, rawData5=$rawData5, rawData6=$rawData6, rawData7=$rawData7, rawData8=$rawData8, rawData9=$rawData9)"
    }

}