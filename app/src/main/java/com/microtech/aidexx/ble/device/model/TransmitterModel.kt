package com.microtech.aidexx.ble.device.model

import android.os.SystemClock
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.MessageObserver
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.entity.CalibrationInfo
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.millisToHours
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.millisToSeconds
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.roundTwoDigits
import com.microtech.aidexx.common.toHistoryDate
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.calibrationBox
import com.microtech.aidexx.db.ObjectBox.cgmHistoryBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.SettingsEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.ui.main.home.HomeStateManager
import com.microtech.aidexx.ui.main.home.glucosePanel
import com.microtech.aidexx.ui.main.home.newOrUsedSensor
import com.microtech.aidexx.ui.main.home.warmingUp
import com.microtech.aidexx.ui.setting.SettingsManager
import com.microtech.aidexx.ui.setting.alert.AlertType
import com.microtech.aidexx.ui.setting.alert.AlertUtil
import com.microtech.aidexx.utils.ByteUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.TimeUtils.dateHourMinute
import com.microtech.aidexx.utils.eventbus.DataChangedType
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.eventbus.EventDataChangedInfo
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.controller.AidexXController
import com.microtechmd.blecomm.entity.BleMessage
import com.microtechmd.blecomm.parser.AidexXCalibrationEntity
import com.microtechmd.blecomm.parser.AidexXFullBroadcastEntity
import com.microtechmd.blecomm.parser.AidexXHistoryEntity
import com.microtechmd.blecomm.parser.AidexXParser
import com.microtechmd.blecomm.parser.AidexXRawHistoryEntity
import io.objectbox.kotlin.equal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import java.util.Date

/**
 * APP-SRC-A-2-7-2
 */
const val TYPE_BRIEF = 1
const val TYPE_RAW = 2

class TransmitterModel private constructor(entity: TransmitterEntity) : DeviceModel(entity) {
    companion object {

        @Synchronized
        fun instance(entity: TransmitterEntity): TransmitterModel {
            val instance = TransmitterModel(entity)
            instance.nextEventIndex = entity.eventIndex + 1
            instance.nextFullEventIndex = entity.fullEventIndex + 1
            instance.nextCalIndex = entity.calIndex + 1
            instance.controller = AidexXController.getInstance()
            instance.controller?.let {
                it.mac = entity.deviceMac
                it.sn = entity.deviceSn
                it.name = "$X_NAME-${entity.deviceSn}"
                val userId = UserInfoManager.instance().userId()
                val getBytes = userId.toByteArray(Charset.forName("UTF-8"))
                it.hostAddress = getBytes
                it.id = entity.accessId
                it.key = entity.encryptionKey
                it.setMessageCallback { operation, success, data ->
                    var resCode = 1
                    var result = data
                    if (operation !in 1..3) {
                        result = ByteUtils.subByte(data, 1, data.size - 1)
                        resCode = data[0].toInt()
                    }
                    val bleMessage =
                        BleMessage(operation, success, result, resCode, entity.messageType)
                    MessageDistributor.instance().send(bleMessage)
                }
            }
            return instance
        }
    }

    override fun observerMessage() {
        MessageDistributor.instance().clear()
        MessageDistributor.instance().observer(object : MessageObserver {
            override fun onMessage(message: BleMessage) {
                this@TransmitterModel.onMessage(message)
            }
        })
    }

    var dataTypeNeedSync = 0
    var isSensorExpired: Boolean = false
    private var newestEventIndex: Int = 0
    private var newestCalIndex: Int = 0
    private var rawRangeStartIndex: Int = 0
    private var briefRangeStartIndex: Int = 0
    private var calRangeStartIndex: Int = 0
    private var lastHyperAlertTime: Long = 0
    private var lastHypoAlertTime: Long = 0
    private var lastUrgentAlertTime: Long = 0
    private var alertSetting: SettingsEntity? = null
    private val cgmHistories: MutableList<RealCgmHistoryEntity> = ArrayList()
    private val tempBriefList = mutableListOf<RealCgmHistoryEntity>()
    private val tempRawList = mutableListOf<RealCgmHistoryEntity>()
    private val tempCalList = mutableListOf<CalibrateEntity>()

    override fun onMessage(message: BleMessage) {
        val data = message.data
        when (message.operation) {
            CgmOperation.DISCOVER -> {
                if (message.isSuccess) {
                    handleAdvertisement(message.data)
                }
            }

            AidexXOperation.GET_START_TIME -> {
                val sensorStartTime = ByteUtils.checkToDate(data)
                sensorStartTime?.let {
                    updateStart(sensorStartTime)
                    ObjectBox.runAsync({
                        transmitterBox!!.put(entity)
                    })
                }
            }

            CgmOperation.GET_DATETIME -> {
            }

            CgmOperation.DISCONNECT -> {
            }

            CgmOperation.CALIBRATION -> {
            }

            CgmOperation.CONFIG_INFO -> {
            }

            CgmOperation.BOND -> {
            }

            CgmOperation.UNPAIR -> {
            }

            AidexXOperation.GET_HISTORY_RANGE -> {
                message.data.let {
                    briefRangeStartIndex =
                        ((it[1].toInt() and 0xff) shl 8).plus((it[0].toInt() and 0xff))
                    rawRangeStartIndex =
                        ((it[3].toInt() and 0xff) shl 8).plus((it[2].toInt() and 0xff))
                    newestEventIndex =
                        ((it[5].toInt() and 0xff) shl 8).plus((it[4].toInt() and 0xff))
                    LogUtil.eAiDEX("get history range ----> brief start:$briefRangeStartIndex, raw start:$rawRangeStartIndex, newest:$newestEventIndex")
                    when (dataTypeNeedSync) {
                        TYPE_BRIEF -> {
                            if (nextEventIndex < briefRangeStartIndex) {
                                nextEventIndex = briefRangeStartIndex
                            }
                            getController().getHistories(nextEventIndex)
                        }

                        TYPE_RAW -> {
                            if (nextFullEventIndex < rawRangeStartIndex) {
                                nextFullEventIndex = rawRangeStartIndex
                            }
                            getController().getRawHistories(nextFullEventIndex)
                        }

                        else -> {}
                    }
                }
            }

            AidexXOperation.GET_CALIBRATION_RANGE -> {
                message.data.let {
                    calRangeStartIndex =
                        ((it[1].toInt() and 0xff) shl 8).plus((it[0].toInt() and 0xff))
                    newestCalIndex = ((it[3].toInt() and 0xff) shl 8).plus((it[2].toInt() and 0xff))
                    LogUtil.eAiDEX("get calibration range --- cal start:$calRangeStartIndex, cal newest:$newestCalIndex")
                }
            }

            AidexXOperation.GET_CALIBRATION -> {
                if (UserInfoManager.instance().isLogin()) {
                    AidexxApp.mainScope.launch(Dispatchers.IO) {
                        saveCalHistory(message.data)
                    }
                }
            }

            AidexXOperation.GET_HISTORIES -> {
                if (UserInfoManager.instance().isLogin()) {
                    saveBriefHistoryFromConnect(message.data)
                }
            }

            AidexXOperation.GET_HISTORIES_RAW -> {
                if (UserInfoManager.instance().isLogin()) {
                    saveRawHistoryFromConnect(message.data)
                }
            }
        }
    }

    override fun getController(): AidexXController {
        return controller as AidexXController
    }

    //更新传感器状态
    fun updateSensorState(insetError: Boolean) {
        entity.needReplace = insetError
        ObjectBox.runAsync({ transmitterBox?.put(entity) })
    }

    override fun isDataValid(): Boolean {
        return (lastHistoryTime != null && glucose != null && !isMalfunction && isHistoryValid && minutesAgo != null && minutesAgo in 0..15)
    }

    fun isDeviceFault(): Boolean {
        return isMalfunction
    }

    fun saveDeviceMode(expirationTime: Int) {
        entity.expirationTime = expirationTime
        transmitterBox!!.put(entity)
    }

    fun clearAccessId() {
        entity.accessId = null
        transmitterBox!!.put(entity)
    }

    override suspend fun uploadPairInfo() {
        LogUtil.eAiDEX("Upload pair info ----> Start time :" + entity.sensorStartTime?.date2ymdhm())
        val map = hashMapOf<String, Any?>()
        map["deviceModel"] = entity.deviceModel
        map["sensorId"] = entity.sensorId
        map["sensorStartUp"] = entity.sensorStartTime
        map["startUpTimeZone"] = TimeUtils.getTimeZoneId()
        map["sensorIndex"] = entity.startTimeToIndex()
        map["deviceSn"] = entity.deviceSn
        map["deviceMac"] = entity.deviceMac
        map["deviceKey"] = entity.encryptionKey
        map["registerTime"] = Date()
        map["et"] = entity.et
        map["isForceReplace"] = true
        val apiResult = ApiService.instance.deviceRegister(map)
        Dialogs.dismissWait()
        when (apiResult) {
            is ApiResult.Success -> {
                apiResult.result.run {
                    this.data?.let {
                        it.deviceId?.let { id ->
                            entity.id = id
                        }
                    }
                }
            }

            is ApiResult.Failure -> {
                EventBusManager.send(EventBusKey.EVENT_PAIR_RESULT, false)
            }
        }
    }

    override fun savePair() {
        entity.encryptionKey = controller?.key
        entity.deviceMac = controller?.mac
        entity.accessId = controller?.id
        entity.deviceName = "$X_NAME-${entity.deviceSn}"
        transmitterBox!!.put(entity)
    }

    fun resetIndex() {
        entity.eventIndex = 0
        entity.fullEventIndex = 0
        nextEventIndex = entity.eventIndex + 1
        nextFullEventIndex = entity.fullEventIndex + 1
    }

    //是否第一次植入传感器
    fun isFirstInsertSensor(): Boolean {
        if (entity.sensorStartTime != null) {
            if ((TimeUtils.currentTimeMillis.millisToSeconds() - entity.sensorStartTime!!.time.millisToSeconds() > 15 * TimeUtils.oneDaySeconds)) {
                return true
            }
        } else {
            if (entity.sensorStartTime != null
                && TimeUtils.currentTimeMillis.millisToSeconds() - entity.sensorStartTime!!.time.millisToSeconds() > 15 * TimeUtils.oneDaySeconds
            ) {
                return true
            }
        }
        return false
    }

    override suspend fun deletePair() {
        if (entity.id != null) {
            when (val apiResult =
                ApiService.instance.deviceUnregister(hashMapOf("deviceId" to entity.id!!))) {
                is ApiResult.Success -> {
                    Dialogs.dismissWait()
                    apiResult.result.run {
                        clearPairInfo()
                    }
                }

                is ApiResult.Failure -> {
                    apiResult.msg.run {
                        Dialogs.showError(this)
                        clearPairInfo()
                    }
                }
            }
        } else {
            clearPairInfo()
        }
    }

    private suspend fun clearPairInfo() {
        controller?.sn = null
        controller?.mac = null
        controller?.id = null
        controller?.key = null
        controller?.unregister()
        entity.accessId = null
        entity.sensorStartTime = null
        entity.id = null
        entity.eventIndex = 0
        entity.fullEventIndex = 0
        entity.calIndex = 0
        TransmitterManager.instance().removeDefault()
        TransmitterManager.instance().removeDb()
        AidexBleAdapter.getInstance().stopBtScan(false)
        EventBusManager.send(EventBusKey.EVENT_UNPAIR_RESULT, true)
    }

    override
    fun handleAdvertisement(data: ByteArray) {
        val broadcast = AidexXParser.getFullBroadcast<AidexXFullBroadcastEntity>(data) ?: return
        LogUtil.eAiDEX("Advertising ----> $broadcast")
        val refreshSensorState = refreshSensorState(broadcast)
        if (refreshSensorState) return
        if (broadcast.calTemp == History.CALIBRATION_NOT_ALLOWED || broadcast.historyTimeOffset < 60 * 6) {
            onCalibrationPermitChange?.invoke(false)
        } else {
            onCalibrationPermitChange?.invoke(true)
        }
        isSensorExpired =
            (broadcast.status == History.SESSION_STOPPED && broadcast.calTemp != History.TIME_SYNCHRONIZATION_REQUIRED)
        isMalfunction =
            broadcast.status == History.SENSOR_MALFUNCTION || broadcast.status == History.DEVICE_SPECIFIC_ALERT || broadcast.status == History.GENERAL_DEVICE_FAULT
        if (isMalfunction) {
            when (broadcast.status) {
                History.SENSOR_MALFUNCTION -> faultType = 1
                History.GENERAL_DEVICE_FAULT -> faultType = 2
            }
        }
        val adHistories = broadcast.history
        latestAd = broadcast
        if (UserInfoManager.shareUserInfo != null) {
            LogUtil.eAiDEX("view sharing")
            return
        }
        if (adHistories.isNotEmpty()) {
            if (latestHistory == null || adHistories[0].timeOffset != latestHistory?.timeOffset) {
                val temp = adHistories[0].glucose
                glucose = if (isMalfunction || isSensorExpired || temp < 0) null
                else temp.toFloat()
            }
            latestHistory = adHistories[0]
            latestHistory?.let {
                if (it.timeOffset > 60) {
                    EventBusManager.send(EventBusKey.UPDATE_NOTIFICATION, true)
                }
            }
        } else {
            return
        }
        isHistoryValid =
            latestHistory?.isValid == 1 && latestHistory?.status == History.STATUS_OK
        glucoseLevel = getGlucoseLevel(glucose)
        if (entity.sensorStartTime == null) {
            getController().startTime
            return
        }
        latestAdTime = SystemClock.elapsedRealtime()
        latestHistory?.let {
            val historyDate = (it.timeOffset).toHistoryDate(entity.sensorStartTime!!)
            if (glucose != null && (lastHistoryTime == null || lastHistoryTime?.time != historyDate.time)) {
                lastHistoryTime = historyDate
            }
        }
        targetEventIndex = latestHistory?.timeOffset ?: 1
        if (nextEventIndex <= targetEventIndex) {
            val broadcastContainsNext = isNextInBroadcast(nextEventIndex, adHistories)
            if (broadcastContainsNext) {
                AidexxApp.mainScope.launch {
                    val historiesFromBroadcast: MutableList<AidexXHistoryEntity>
                    withContext(Dispatchers.IO) {
                        historiesFromBroadcast =
                            getHistoriesFromBroadcast(nextEventIndex, adHistories)
                    }
                    if (historiesFromBroadcast.isNotEmpty()) {
                        alertSetting = SettingsManager.getSettings()
                        saveBriefHistory(historiesFromBroadcast.asReversed(), false)
                    }
                }
            } else {
                latestHistory?.let {
                    if (targetEventIndex > nextEventIndex) {
                        if (newestEventIndex == targetEventIndex) {
                            if (nextEventIndex < briefRangeStartIndex) {
                                nextEventIndex = briefRangeStartIndex
                            }
                            getController().getHistories(nextEventIndex)
                        } else {
                            dataTypeNeedSync = TYPE_BRIEF
                            getController().historyRange
                        }
                    }
                }
            }
            return
        }
        val numGetHistory = 30
        if (targetEventIndex >= nextFullEventIndex + numGetHistory
            || ((targetEventIndex >= nextFullEventIndex) && isSensorExpired)
        ) {
            if (newestEventIndex == targetEventIndex) {
                if (nextFullEventIndex < rawRangeStartIndex) {
                    nextFullEventIndex = rawRangeStartIndex
                }
                getController().getRawHistories(nextFullEventIndex)
            } else {
                dataTypeNeedSync = TYPE_RAW
                getController().historyRange
            }
            return
        }
        val calTimeOffset = broadcast.calTimeOffset
        if (nextCalIndex <= calTimeOffset) {
            if (newestCalIndex == calTimeOffset) {
                if (nextCalIndex < calRangeStartIndex) {
                    nextCalIndex = calRangeStartIndex
                }
                getController().getCalibration(nextCalIndex)
            } else {
                getController().calibrationRange
            }
        }
    }

    override fun getSensorRemainingTime(): Int? {
        val days = entity.expirationTime
        return when {
            isSensorExpired -> -1
            entity.sensorStartTime == null || latestAdTime == 0L || (SystemClock.elapsedRealtime() - latestAdTime).millisToMinutes() > 15 -> null
            else -> {
                (days * TimeUtils.oneDayMillis - (TimeUtils.currentTimeMillis - entity.sensorStartTime?.time!!)).millisToHours()
            }
        }
    }

    override fun calibration(info: CalibrationInfo) {
        getController().calibration(info.intValue, info.timeOffset)
    }

    override fun isAllowCalibration(): Boolean {
        latestAd?.let {
            val entity = latestAd as AidexXFullBroadcastEntity
            return entity.calTemp != History.CALIBRATION_NOT_ALLOWED && entity.historyTimeOffset > 60 * 6
        }
        return false
    }

    private fun refreshSensorState(broadcast: AidexXFullBroadcastEntity): Boolean {
        broadcast.let {
            if (it.status == History.SESSION_STOPPED && it.calTemp == History.TIME_SYNCHRONIZATION_REQUIRED) {
                HomeStateManager.instance().setState(newOrUsedSensor)
                return true
            } else if (it.historyTimeOffset in 0..59 && it.isPaired == 1) {
                HomeStateManager.instance().setState(warmingUp)
                HomeStateManager.instance().setWarmingUpTimeLeft(it.historyTimeOffset)
            } else {
                HomeStateManager.instance().setState(glucosePanel)
            }
        }
        return false
    }

    private fun isNextInBroadcast(next: Int, histories: List<AidexXHistoryEntity>): Boolean {
        return next in histories[histories.size - 1].timeOffset..histories[0].timeOffset
    }

    private fun getHistoriesFromBroadcast(
        next: Int, list: MutableList<AidexXHistoryEntity>
    ): MutableList<AidexXHistoryEntity> {
        var startIndex = 0
        for ((index, history) in list.withIndex()) {
            if (history.timeOffset == next) {
                startIndex = index + 1
                break
            }
        }
        return list.subList(0, startIndex)
    }

    override fun saveBriefHistoryFromConnect(data: ByteArray) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val histories = AidexXParser.getHistories<AidexXHistoryEntity>(data)
            if (histories.isNullOrEmpty()) return@launch
            alertSetting = SettingsManager.getSettings()
            if (histories.first().timeOffset == nextEventIndex) {
                saveBriefHistory(histories)
            }
        }
    }

    override fun saveRawHistoryFromConnect(data: ByteArray) {
        AidexxApp.mainScope.launch(Dispatchers.IO) {
            val histories = AidexXParser.getRawHistory<AidexXRawHistoryEntity>(data)
            if (histories.isEmpty()) return@launch
            if (histories.first().timeOffset == nextFullEventIndex) {
                saveRawHistory(histories)
            }
        }
    }

    private fun saveCalHistory(data: ByteArray?) {
        val aidexXCalibration = AidexXParser.getAidexXCalibration<AidexXCalibrationEntity>(data)
        val userId = UserInfoManager.instance().userId()
        if (aidexXCalibration.isEmpty() || entity.sensorId.isNullOrEmpty() || userId.isEmpty()) {
            LogUtil.eAiDEX("Save calibration history error")
            return
        }
        ObjectBox.runAsync({
            tempCalList.clear()
            for (calibration in aidexXCalibration) {
                val calibrateEntity = CalibrateEntity()
                calibrateEntity.sensorId = entity.sensorId
                calibrateEntity.index = calibration.index
                calibrateEntity.timeOffset = calibration.timeOffset
                val historyDate = (calibration.timeOffset).toHistoryDate(entity.sensorStartTime!!)
                calibrateEntity.setTimeInfo(historyDate)
                calibrateEntity.userId = userId
                calibrateEntity.calibrationId = calibrateEntity.updateCalibrationId()
                calibrateEntity.cf = (calibration.cf / 100).roundTwoDigits()
                calibrateEntity.offset = (calibration.offset / 100).roundTwoDigits()
                calibrateEntity.referenceGlucose = calibration.referenceGlucose
                calibrateEntity.isValid = calibration.isValid
                calibrateEntity.uploadState = 1
                tempCalList.add(calibrateEntity)
            }
            if (tempCalList.isNotEmpty()) {
                calibrationBox!!.put(tempCalList)
            }
        }, {
            entity.calIndex = aidexXCalibration.last().index
            nextCalIndex = entity.calIndex + 1
            transmitterBox!!.put(entity)
            EventBusManager.send(
                EventBusKey.EVENT_DATA_CHANGED,
                EventDataChangedInfo(DataChangedType.ADD, mutableListOf<CalibrateEntity>().also {
                    it.addAll(tempCalList)
                })
            )
            tempCalList.clear()
            continueCalFetch()
        }, {
            tempCalList.clear()
        })
    }

    private fun continueCalFetch() {
        if (newestCalIndex > nextCalIndex) {
            getController().getCalibration(nextCalIndex)
            isGettingTransmitterData = true
        }
    }

    // 保存数据
    private fun saveBriefHistory(
        histories: MutableList<AidexXHistoryEntity>,
        goon: Boolean = true
    ) {
        val userId = UserInfoManager.instance().userId()
        val sensorId = entity.sensorId
        if (userId.isEmpty() || sensorId.isNullOrEmpty()) return
        ObjectBox.runAsync({
            val now = TimeUtils.currentTimeMillis
            val alertFrequency = AlertUtil.alertFrequency
            val urgentFrequency = AlertUtil.urgentFrequency
            tempBriefList.clear()
            for (history in histories) {
                val historyEntity = RealCgmHistoryEntity()
                if (history.isValid == 0) {
                    historyEntity.eventType = History.HISTORY_INVALID
                } else {
                    when (history.status) {
                        History.STATUS_OK -> {
                            historyEntity.eventType = History.HISTORY_GLUCOSE
                        }

                        History.STATUS_INVALID -> {
                            historyEntity.eventType = History.HISTORY_GLUCOSE_INVALID
                        }

                        History.STATUS_ERROR -> {
                            historyEntity.eventType = History.HISTORY_SENSOR_ERROR
                        }
                    }
                }
                historyEntity.timeOffset = history.timeOffset
                val historyDate = (history.timeOffset).toHistoryDate(entity.sensorStartTime!!)
                historyEntity.deviceTime = historyDate
                historyEntity.sensorId = sensorId
                historyEntity.sensorIndex = entity.startTimeToIndex()
                historyEntity.userId = userId
                historyEntity.status = history.status
                historyEntity.quality = history.quality
                historyEntity.glucoseIsValid = history.isValid
                val recordUuid = historyEntity.updateRecordUUID()
                historyEntity.frontRecordId = recordUuid
                historyEntity.briefUploadState = 1
                val time = historyDate.dateHourMinute()
                historyEntity.glucose = history.glucose.toFloat()
                val deviceTimeMillis = historyEntity.deviceTime.time
                when (historyEntity.eventType) {
                    History.HISTORY_GLUCOSE,
                    -> {
                        if (isMalfunction || history.timeOffset < 60) {
                            historyEntity.eventWarning = -1
                        } else {
                            val highOrLowGlucoseType = historyEntity.getHighOrLowGlucoseType()
                            if (highOrLowGlucoseType != 0) {
                                when (highOrLowGlucoseType) {
                                    History.HISTORY_LOCAL_HYPER -> {
                                        if (AlertUtil.hyperSwitchEnable) {
                                            if (lastHyperAlertTime == 0L) {
                                                lastHyperAlertTime =
                                                    getLastAlertTime(
                                                        sensorId,
                                                        History.HISTORY_LOCAL_HYPER
                                                    )
                                            }
                                            if ((lastHyperAlertTime == 0L
                                                        || deviceTimeMillis - lastHyperAlertTime > alertFrequency)
                                                && TimeUtils.currentTimeMillis - deviceTimeMillis <= alertFrequency
                                            ) {
                                                historyEntity.eventWarning =
                                                    History.HISTORY_LOCAL_HYPER
                                                alert?.invoke(
                                                    "$time", AlertType.MESSAGE_TYPE_GLUCOSEHIGH
                                                )
                                                lastHyperAlertTime = deviceTimeMillis
                                            }
                                        }
                                    }

                                    History.HISTORY_LOCAL_HYPO -> {
                                        if (AlertUtil.hypoSwitchEnable) {
                                            if (lastHypoAlertTime == 0L) {
                                                lastHypoAlertTime = getLastAlertTime(
                                                    sensorId, History.HISTORY_LOCAL_HYPO
                                                )
                                            }
                                            if ((lastHypoAlertTime == 0L
                                                        || deviceTimeMillis - lastHypoAlertTime > alertFrequency)
                                                && TimeUtils.currentTimeMillis - deviceTimeMillis <= alertFrequency
                                            ) {
                                                historyEntity.eventWarning =
                                                    History.HISTORY_LOCAL_HYPO
                                                alert?.invoke(
                                                    "$time", AlertType.MESSAGE_TYPE_GLUCOSELOW
                                                )
                                                lastHypoAlertTime = deviceTimeMillis
                                            }
                                        }
                                    }

                                    History.HISTORY_LOCAL_URGENT_HYPO -> {
                                        if (AlertUtil.urgentLowSwitchEnable) {
                                            if (lastUrgentAlertTime == 0L) {
                                                lastUrgentAlertTime =
                                                    getLastAlertTime(
                                                        sensorId,
                                                        History.HISTORY_LOCAL_URGENT_HYPO
                                                    )
                                            }
                                            if ((lastUrgentAlertTime == 0L
                                                        || deviceTimeMillis - lastUrgentAlertTime > urgentFrequency)
                                                && TimeUtils.currentTimeMillis - deviceTimeMillis <= urgentFrequency
                                            ) {
                                                historyEntity.eventWarning =
                                                    History.HISTORY_LOCAL_URGENT_HYPO
                                                alert?.invoke(
                                                    "$time", AlertType.MESSAGE_TYPE_GLUCOSELOWALERT
                                                )
                                                lastUrgentAlertTime = deviceTimeMillis
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    History.HISTORY_SENSOR_ERROR -> {
                        if (entity.needReplace && now - deviceTimeMillis < TimeUtils.oneHourSeconds * 1000) {
                            alert?.invoke(
                                "$time", AlertType.MESSAGE_TYPE_SENRORERROR
                            )
                        }
                    }
                }
                tempBriefList.add(historyEntity)
            }
            if (tempBriefList.isNotEmpty()) {
                cgmHistoryBox!!.put(tempBriefList)
            }
            LogUtil.eAiDEX("Save ${histories.size} brief histories takes : ${TimeUtils.currentTimeMillis - now} ms")
        }, onSuccess = {
            entity.eventIndex = histories.last().timeOffset
            nextEventIndex = entity.eventIndex + 1
            transmitterBox!!.put(entity)
//                updateGlucoseTrend(tempBriefList.last().deviceTime)
            EventBusManager.send(
                EventBusKey.EVENT_DATA_CHANGED,
                EventDataChangedInfo(
                    DataChangedType.ADD,
                    mutableListOf<RealCgmHistoryEntity>().also {
                        it.addAll(tempBriefList)
                    })
            )
            tempBriefList.clear()
            if (goon) continueBriefFetch()
        }, onError = {
            tempBriefList.clear()
        })
    }

    private fun continueBriefFetch() {
        if (targetEventIndex >= nextEventIndex) {
            if (nextEventIndex < briefRangeStartIndex) {
                nextEventIndex = briefRangeStartIndex
            }
            getController().getHistories(nextEventIndex)
            isGettingTransmitterData = true
        } else if (targetEventIndex >= nextFullEventIndex) {
            if (nextFullEventIndex < rawRangeStartIndex) {
                nextFullEventIndex = rawRangeStartIndex
            }
            getController().getRawHistories(nextFullEventIndex)
            isGettingTransmitterData = true
        }
    }

    private fun continueRawFetch() {
        if (targetEventIndex > nextFullEventIndex) {
            getController().getRawHistories(nextFullEventIndex)
            isGettingTransmitterData = true
        }
    }

    private fun getLastAlertTime(sensorId: String, type: Int): Long {
        val build = cgmHistoryBox!!.query().equal(
            RealCgmHistoryEntity_.sensorId, sensorId
        )
        when (type) {
            History.HISTORY_LOCAL_HYPER -> build.equal(
                RealCgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_HYPER
            )

            History.HISTORY_LOCAL_HYPO -> build.equal(
                RealCgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_HYPO
            )

            History.HISTORY_LOCAL_URGENT_HYPO -> build.equal(
                RealCgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_URGENT_HYPO
            )
        }
        val lastAlert = build.orderDesc(RealCgmHistoryEntity_.idx).build().findFirst()
        return lastAlert?.deviceTime?.time ?: 0
    }

    private fun saveRawHistory(rawHistories: List<AidexXRawHistoryEntity>) {
        val userId = UserInfoManager.instance().userId()
        if (userId.isEmpty() || entity.sensorId.isNullOrEmpty()) {
            return
        }
        ObjectBox.runAsync({
            tempRawList.clear()
            for (rawHistory in rawHistories) {
                val existHistory = cgmHistoryBox!!.query()
                    .equal(RealCgmHistoryEntity_.timeOffset, rawHistory.timeOffset)
                    .equal(RealCgmHistoryEntity_.sensorId, entity.sensorId!!)
                    .orderDesc(RealCgmHistoryEntity_.idx).build().findFirst()
                if (existHistory != null) {
                    existHistory.rawOne = rawHistory.i1
                    existHistory.rawTwo = rawHistory.i2
                    existHistory.rawVc = rawHistory.vc
                    existHistory.rawIsValid = rawHistory.isValid
                    existHistory.rawUploadState = 1
                    tempRawList.add(existHistory)
                }
            }
            if (tempRawList.isNotEmpty()) {
                cgmHistoryBox!!.put(tempRawList)
            }
        }, onSuccess = {
            entity.fullEventIndex = rawHistories.last().timeOffset
            nextFullEventIndex = entity.fullEventIndex + 1
            transmitterBox!!.put(entity)
            tempRawList.clear()
            continueRawFetch()
        }, onError = {
            tempRawList.clear()
        })
    }

    private fun getGlucoseLevel(glucose: Float?): GlucoseLevel? {
        return when {
            glucose == null -> null
            glucose > ThresholdManager.hyper -> GlucoseLevel.HIGH
            glucose < ThresholdManager.hypo -> GlucoseLevel.LOW
            else -> GlucoseLevel.NORMAL
        }
    }
}