package com.microtech.aidexx.ble.device.model

import android.os.SystemClock
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.ble.MessageDispatcher
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.entity.CalibrationInfo
import com.microtech.aidexx.common.millisToHours
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.millisToSeconds
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.cgmHistoryBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.db.entity.TransmitterEntity_
import com.microtech.aidexx.ui.main.home.HomeStateManager
import com.microtech.aidexx.ui.main.home.glucosePanel
import com.microtech.aidexx.ui.main.home.newOrUsedSensor
import com.microtech.aidexx.ui.main.home.warmingUp
import com.microtech.aidexx.ui.setting.alert.AlertManager
import com.microtech.aidexx.ui.setting.alert.AlertManager.Companion.calculateFrequency
import com.microtech.aidexx.ui.setting.alert.AlertType
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.utils.TimeUtils.dateHourMinute
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.controller.AidexXController
import com.microtechmd.blecomm.entity.BleMessage
import com.microtechmd.blecomm.parser.AidexXBroadcastEntity
import com.microtechmd.blecomm.parser.AidexXFullBroadcastEntity
import com.microtechmd.blecomm.parser.AidexXHistoryEntity
import com.microtechmd.blecomm.parser.AidexXParser
import com.microtechmd.blecomm.parser.AidexXRawHistoryEntity
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder
import java.math.RoundingMode
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt


/**
 * APP-SRC-A-2-7-2
 */
class TransmitterModel private constructor(entity: TransmitterEntity) : DeviceModel(entity) {
    companion object {
        var notifyNotification: (() -> Unit)? = null
        var messageCallBack: ((msg: BleMessage) -> Unit)? = null
        var alert: ((time: String, type: Int) -> Unit)? = null

        private var instance: TransmitterModel? = null

        @Synchronized
        fun instance(entity: TransmitterEntity): TransmitterModel {
            if (instance == null || instance?.entity?.deviceSn != entity.deviceSn) {
                instance = TransmitterModel(entity)
            }
            if (instance!!.controller == null) {
                instance!!.controller = AidexXController()
            }
            instance!!.controller?.let {
                it.mac = entity.deviceMac
                it.sn = entity.deviceSn
                it.name = "AiDEX X-${entity.deviceSn}"
                val userId = UserInfoManager.instance().userId()
                val getBytes = userId.toByteArray(Charset.forName("UTF-8"))
                it.hostAddress = getBytes
                it.id = entity.accessId
                it.key = entity.encryptionKey
                it.setMessageCallback { operation, success, data ->
                    LogUtil.eAiDEX(
                        "model --- operation: $operation, success: $success, message: ${
                            StringUtils.binaryToHexString(data)
                        }"
                    )
                    var result = data
                    if (operation !in 1..3) {
                        result = ByteUtils.subByte(data, 1, data.size - 1);
                    }
                    val bleMessage = BleMessage(operation, success, result)
                    instance?.onMessage(bleMessage)
                    MessageDispatcher.instance().dispatch(AidexxApp.mainScope, bleMessage)
                }
            }
            return instance!!
        }
    }

    private val typeHyperAlert = 1
    private val typeHypoAlert = 2
    private val typeUrgentAlert = 3
    private var newestIndex: Int = 0
    var isSensorExpired: Boolean = false
    private var rawRangeStartIndex: Int = 0
    private var briefRangeStartIndex: Int = 0
    private var lastHyperAlertTime: Long? = null
    private var lastHypoAlertTime: Long? = null
    private var lastUrgentAlertTime: Long? = null
    private val cgmHistories: MutableList<RealCgmHistoryEntity> = ArrayList()
    private val tempBriefList = mutableListOf<RealCgmHistoryEntity>()
    private val tempRawList = mutableListOf<RealCgmHistoryEntity>()

    fun onMessage(message: BleMessage) {
        val data = message.data
        when (message.operation) {
            CgmOperation.DISCOVER -> {
                if (message.isSuccess) {
                    handleAdvertisement(message.data)
                }
            }
            AidexXOperation.GET_START_TIME -> {
                if (!AidexxApp.isPairing) {
                    val sensorStartTime = ByteUtils.toDate(data)
                    updateStartTime(sensorStartTime)
                }
            }
            CgmOperation.GET_DATETIME -> {
                disconnect()
            }

            CgmOperation.CALIBRATION -> {

            }

            CgmOperation.CONFIG_INFO -> {

            }

            CgmOperation.BOND -> {

            }

            CgmOperation.UNPAIR -> {

            }
            CgmOperation.GET_HISTORIES -> {
                if (UserInfoManager.instance().isLogin()) {
                    saveBriefHistoryFromConnect(message.data)
                }
            }
            CgmOperation.GET_HISTORIES_FULL -> {
                if (UserInfoManager.instance().isLogin()) {
                    saveRawHistoryFromConnect(message.data)
                }
            }
        }
        LogUtil.eAiDEX(
            "mainservice --- operation: ${message.operation}, message: ${
                StringUtils.binaryToHexString(data)
            }"
        )
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
        return (lastHistoryTime != null && minutesAgo != null && minutesAgo in 0..15 && glucose != null && !isMalfunction && isHistoryValid)
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

    override suspend fun savePair(
        model: Int,
        version: String?,
        success: (() -> Unit)?,
        fail: (() -> Unit)?
    ) {
        entity.encryptionKey = controller?.key
        entity.deviceMac = controller?.mac
        entity.accessId = controller?.id
        entity.deviceModel = model
        entity.version = version
        entity.deviceName = controller?.name
        entity.deviceSn?.let {
            val existTrans = ObjectBox.store.awaitCallInTx {
                transmitterBox!!.query()
                    .equal(
                        TransmitterEntity_.deviceSn, it,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                    .build()
                    .findFirst()
            }
            if (existTrans != null) {
                entity.eventIndex = existTrans.eventIndex
                entity.fullEventIndex = existTrans.fullEventIndex
                entity.id = existTrans.id
                entity.idx = existTrans.idx
            }
            targetEventIndex = entity.eventIndex
            nextEventIndex = entity.eventIndex + 1
            nextFullEventIndex = entity.fullEventIndex + 1
        }
        //向服务器请求注册设备
        when (val apiResult = ApiService.instance.deviceRegister(entity)) {
            is ApiResult.Success -> {
                apiResult.result.run {
                    this.content?.let {
                        entity.id = it.id
                        entity.deviceSn = it.deviceSn
                        entity.eventIndex = it.eventIndex
                        entity.fullEventIndex = it.fullEventIndex
                    }
                    entity.accessId = controller?.id
                    targetEventIndex = entity.eventIndex
                    nextEventIndex = entity.eventIndex + 1
                    nextFullEventIndex = entity.fullEventIndex + 1
                    ObjectBox.runAsync({
                        transmitterBox!!.removeAll()
                        transmitterBox!!.put(entity)
                    }, {
                        TransmitterManager.instance().set(this@TransmitterModel)
                        success?.invoke()
                    })
                }
            }
            is ApiResult.Failure -> {
                apiResult.msg.run {
                    TransmitterManager.instance().removeDefault()
                    fail?.invoke()
                }
            }
        }
    }

    fun resetIndex() {
        entity.eventIndex = 0
        entity.fullEventIndex = 0
        nextEventIndex = entity.eventIndex + 1
        nextFullEventIndex = entity.fullEventIndex + 1
    }

    //是否第一次植入传感器
    fun isFirstInsertSensor(): Boolean {
        if (sensorStartTime != null) {
            if ((TimeUtils.currentTimeMillis.millisToSeconds() - sensorStartTime!!.time.millisToSeconds() > 15 * TimeUtils.oneDaySeconds)) {
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
        entity.id?.let {
            when (val apiResult = ApiService.instance.deviceUnregister(hashMapOf("id" to it))) {
                is ApiResult.Success -> {
                    apiResult.result.run {
                        TransmitterManager.instance().removeDb()
                        TransmitterManager.instance().removeDefault()
                        entity.accessId = null
                        controller?.sn = null
                        controller?.mac = null
                        controller?.id = null
                        controller?.unregister()
                        EventBusManager.send(EventBusKey.EVENT_UNPAIR_SUCCESS, true)
                    }
                }
                is ApiResult.Failure -> {
                    apiResult.msg.run {
                        Dialogs.showError(this)
                        EventBusManager.send(EventBusKey.EVENT_UNPAIR_SUCCESS, false)
                    }
                }
            }
        }
    }

    private fun getHistoryDate(timeOffset: Int): Date {
        val timeLong = entity.sensorStartTime?.time?.plus(timeOffset * 60 * 1000)
        return Date(timeLong!!)
    }

    override
    fun handleAdvertisement(data: ByteArray) {
        val broadcast = AidexXParser.getFullBroadcast<AidexXFullBroadcastEntity>(data)
        if (entity.sensorStartTime == null) {
            getController().startTime
            return
        }
        broadcast?.let {
            refreshSensorState(broadcast)
        }
        if (broadcast.calTemp == 0 || broadcast.calTemp == History.CALIBRATION_NOT_ALLOWED || broadcast.historyTimeOffset < 60 * 6) {
            onCalibrationPermitChange?.invoke(false)
        } else {
            onCalibrationPermitChange?.invoke(true)
        }
        isSensorExpired =
            (broadcast.status == History.SESSION_STOPPED && broadcast.calTemp != History.TIME_SYNCHRONIZATION_REQUIRED)
        isMalfunction =
            broadcast.status == History.SENSOR_MALFUNCTION || broadcast.status == History.DEVICE_SPECIFIC_ALERT || broadcast.status == History.GENERAL_DEVICE_FAULT
        if (broadcast.status == History.SENSOR_MALFUNCTION) {
            faultType = 1
        } else if (broadcast.status == History.GENERAL_DEVICE_FAULT) {
            faultType = 2
        }
        val adHistories = broadcast.history
        if (adHistories.isNotEmpty()) {
            latestHistory = adHistories[0]
        } else {
            return
        }
        isHistoryValid =
            latestHistory?.isValid == 1 && latestHistory?.status == History.STATUS_OK
        latestAd = broadcast
        latestAdTime = SystemClock.elapsedRealtime()
        if (UserInfoManager.shareUserInfo != null) {
            LogUtil.eAiDEX("view sharing")
            return
        }
        val temp = broadcast.history[0].glucose
        glucose = if (isMalfunction || isSensorExpired || temp < 0) null
        else com.microtech.aidexx.widget.dialog.lib.util.roundOffDecimal(temp / 18f)
        glucoseLevel = getGlucoseLevel(glucose)
        latestHistory?.let {
            val historyDate = getHistoryDate(it.timeOffset)
            if (glucose != null && lastHistoryTime != historyDate) {
                lastHistoryTime = historyDate
            }
        }
        targetEventIndex = latestHistory?.timeOffset ?: 0
        if (nextEventIndex <= targetEventIndex) {
            val broadcastContainsNext = isNextInBroadcast(nextEventIndex, adHistories)
            if (broadcastContainsNext) {
                val historiesFromBroadcast =
                    getHistoriesFromBroadcast(nextEventIndex, adHistories)
                saveBriefHistory(historiesFromBroadcast.reversed())
            } else {
                latestHistory?.let {
                    if (targetEventIndex > nextEventIndex) {
                        if (newestIndex == targetEventIndex) {
                            if (nextEventIndex < briefRangeStartIndex) {
                                nextEventIndex = briefRangeStartIndex
                            }
                            getController().getHistories(nextEventIndex)
                        } else {
                            getController().historyRange
                        }
                    }
                }
            }
            return
        }
        val numGetHistory = 40
        if (targetEventIndex >= nextFullEventIndex + numGetHistory
            || ((targetEventIndex >= nextFullEventIndex) && isSensorExpired)
        ) {
            if (newestIndex == targetEventIndex) {
                if (nextFullEventIndex < rawRangeStartIndex) {
                    nextFullEventIndex = rawRangeStartIndex
                }
                getController().getRawHistories(nextFullEventIndex)
            } else {
                getController().historyRange
            }
        }
    }

    override fun getSensorRemainingTime(): Int? {
        val days = entity.expirationTime
        return when {
            isSensorExpired -> 0
            entity.sensorStartTime == null || latestAdTime == 0L || (SystemClock.elapsedRealtime() - latestAdTime).millisToMinutes() > 15 -> null
            else -> {
                (days * TimeUtils.oneDaySeconds - (TimeUtils.currentTimeMillis - entity.sensorStartTime?.time!!).millisToSeconds()).millisToHours()
            }
        }
    }

    override fun calibration(info: CalibrationInfo) {
        getController().calibration(info.intValue, info.timeOffset)
    }

    override fun isAllowCalibration(): Boolean {
        latestAd?.let {
            val entity = latestAd as AidexXBroadcastEntity
            return entity.calTemp != History.CALIBRATION_NOT_ALLOWED && entity.timeOffset > 60 * 6
        }
        return false
    }

    private fun refreshSensorState(broadcast: AidexXFullBroadcastEntity) {
        broadcast.let {
            if (it.status == History.SESSION_STOPPED && it.calTemp == History.TIME_SYNCHRONIZATION_REQUIRED) {
                HomeStateManager.instance().setState(newOrUsedSensor)
            } else if (it.historyTimeOffset < 60) {
                HomeStateManager.instance().setState(warmingUp)
                HomeStateManager.instance().setWarmingUpTimeLeft(it.historyTimeOffset)
            } else {
                HomeStateManager.instance().setState(glucosePanel)
            }
        }
    }

    private fun isNextInBroadcast(next: Int, histories: List<AidexXHistoryEntity>): Boolean {
        return next in histories[histories.size - 1].timeOffset..histories[0].timeOffset
    }

    private fun getHistoriesFromBroadcast(
        next: Int, list: MutableList<AidexXHistoryEntity>
    ): List<AidexXHistoryEntity> {
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
        val histories = AidexXParser.getHistories<AidexXHistoryEntity>(data)
        if (histories.isNullOrEmpty()) return
        if (histories.first().timeOffset == nextEventIndex) {
            saveBriefHistory(histories)
        }
    }

    override fun saveRawHistoryFromConnect(data: ByteArray) {
        val histories = AidexXParser.getRawHistory<AidexXRawHistoryEntity>(data)
        if (histories.isEmpty()) return
        if (histories.first().timeOffset == nextFullEventIndex) {
            saveRawHistory(histories)
        }
    }

    fun roundOffDecimal(number: Float): Float {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toFloat()
    }

    // 保存数据
    private fun saveBriefHistory(histories: List<AidexXHistoryEntity>) {
        tempBriefList.clear()
        val deviceId = TransmitterManager.instance().getDefault()?.deviceId() ?: return
        val userId = UserInfoManager.instance().userId()
        if (userId.isEmpty()) {
            return
        }
        ObjectBox.runAsync({
            val now = TimeUtils.currentTimeMillis
            for (history in histories) {
                val oldHistory = cgmHistoryBox!!.query().equal(
                    RealCgmHistoryEntity_.sensorIndex,
                    entity.startTimeToIndex()
                ).equal(RealCgmHistoryEntity_.eventIndex, history.timeOffset).equal(
                    RealCgmHistoryEntity_.deviceId,
                    deviceId,
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).equal(
                    RealCgmHistoryEntity_.authorizationId,
                    userId,
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).orderDesc(RealCgmHistoryEntity_.idx).build().findFirst()

                if (oldHistory != null) {
                    LogUtil.eAiDEX("History exist,need not update}")
                    continue
                }
                val time = getHistoryDate(history.timeOffset).dateHourMinute()
                val historyEntity = RealCgmHistoryEntity()
                historyEntity.deviceId = deviceId
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
                historyEntity.eventData =
                    com.microtech.aidexx.widget.dialog.lib.util.roundOffDecimal(history.glucose / 18f)
                historyEntity.eventIndex = history.timeOffset
                historyEntity.time = getHistoryDate(history.timeOffset)
                historyEntity.deviceTime = getHistoryDate(history.timeOffset)
                historyEntity.sensorIndex =
                    (entity.sensorStartTime?.time!!).toInt()
                if (history.timeOffset < 60) {
                    historyEntity.eventWarning = -1
                }
                when (historyEntity.eventType) {
                    History.HISTORY_GLUCOSE,
                    -> {
                        if (isMalfunction || historyEntity.eventWarning == -1) {
                            historyEntity.eventWarning = -1
                        } else {
                            if (historyEntity.isHighOrLow()) {
                                when {
                                    historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_HYPER -> {
                                        if (MmkvManager.isHyperAlertEnable()) if (lastHyperAlertTime == null) {
                                            lastHyperAlertTime = getLastAlertTime(
                                                deviceId, userId, typeHyperAlert
                                            )
                                        }
                                        if (lastHyperAlertTime == null || historyEntity.deviceTime.time - lastHyperAlertTime!!
                                            > AlertManager.calculateFrequency(MmkvManager.getAlertFrequency())
                                        ) {
                                            historyEntity.updateEventWarning()
                                            alert?.invoke(
                                                "$time", AlertType.MESSAGE_TYPE_GLUCOSEHIGH
                                            )
                                            lastHyperAlertTime = historyEntity.deviceTime.time
                                        }
                                    }
                                    historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_HYPO -> {
                                        if (MmkvManager.isHypoAlertEnable()) if (lastHypoAlertTime == null) {
                                            lastHypoAlertTime = getLastAlertTime(
                                                deviceId, userId, typeHypoAlert
                                            )
                                        }
                                        if (lastHypoAlertTime == null || historyEntity.deviceTime.time - lastHypoAlertTime!!
                                            > AlertManager.calculateFrequency(MmkvManager.getAlertFrequency())
                                        ) {
                                            historyEntity.updateEventWarning()
                                            alert?.invoke(
                                                "$time", AlertType.MESSAGE_TYPE_GLUCOSELOW
                                            )
                                            lastHypoAlertTime = historyEntity.deviceTime.time
                                        }
                                    }
                                    historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_URGENT_HYPO -> {
                                        if (MmkvManager.isHypoAlertEnable()) if (lastUrgentAlertTime == null) {
                                            lastUrgentAlertTime = getLastAlertTime(
                                                deviceId, userId, typeUrgentAlert
                                            )
                                        }
                                        if (lastUrgentAlertTime == null || historyEntity.deviceTime.time - lastUrgentAlertTime!!
                                            > AlertManager.calculateFrequency(MmkvManager.getUrgentAlertFrequency())
                                        ) {
                                            historyEntity.updateEventWarning()
                                            alert?.invoke(
                                                "$time", AlertType.MESSAGE_TYPE_GLUCOSELOWALERT
                                            )
                                            lastUrgentAlertTime = historyEntity.deviceTime.time
                                        }
                                    }
                                }
                            }
                        }
                    }
                    History.HISTORY_SENSOR_ERROR -> {
                        if (now - historyEntity.deviceTime.time < 1000 * 60 * 30 && entity.needReplace) {
                            alert?.invoke(
                                "$time", AlertType.MESSAGE_TYPE_SENRORERROR
                            )
                        }
                    }
                }
                historyEntity.authorizationId = userId
                historyEntity.updateRecordUUID()
                tempBriefList.add(historyEntity)
            }
            cgmHistoryBox!!.put(tempBriefList)
        }, onSuccess = {
            if (UserInfoManager.instance().isLogin()) {
                cgmHistories.addAll(tempBriefList)
                if (UserInfoManager.shareUserInfo == null) {
                    TransmitterManager.instance().updateHistories(tempBriefList)
                }
                tempBriefList.sortBy { it.eventIndex }
                entity.eventIndex = tempBriefList.last().eventIndex
                nextEventIndex = entity.eventIndex + 1
                transmitterBox!!.put(entity)
                updateGlucoseTrend(tempBriefList.last().deviceTime)
                tempBriefList.clear()
                continueBriefFetch()
            }
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
        } else if (targetEventIndex >= nextFullEventIndex) {
            if (nextFullEventIndex < rawRangeStartIndex) {
                nextFullEventIndex = rawRangeStartIndex
            }
            getController().getRawHistories(nextFullEventIndex)
        } else {
            getController().disconnect()
        }
    }

    private fun continueRawFetch() {
        if (targetEventIndex > nextFullEventIndex) {
            getController().getRawHistories(nextFullEventIndex)
        } else {
            getController().disconnect()
        }
    }

    private fun getLastAlertTime(deviceId: String, userId: String, type: Int): Long? {
        val build = cgmHistoryBox!!.query().equal(
            RealCgmHistoryEntity_.sensorIndex, entity.sensorIndex
        ).equal(
            RealCgmHistoryEntity_.deviceId, deviceId, QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).equal(
            RealCgmHistoryEntity_.authorizationId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE
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
        return lastAlert?.deviceTime?.time
    }

    private fun saveRawHistory(rawHistories: List<AidexXRawHistoryEntity>) {
        val deviceId = TransmitterManager.instance().getDefault()?.deviceId() ?: return
        val userId = UserInfoManager.instance().userId()
        if (userId.isEmpty()) {
            return
        }
        tempRawList.clear()
        ObjectBox.runAsync({
            for (rawHistory in rawHistories) {
                val existHistory = cgmHistoryBox!!.query()
                    .equal(RealCgmHistoryEntity_.eventIndex, rawHistory.timeOffset)
                    .equal(
                        RealCgmHistoryEntity_.deviceId,
                        deviceId,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).equal(
                        RealCgmHistoryEntity_.authorizationId,
                        userId,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).orderDesc(RealCgmHistoryEntity_.idx).build().findFirst()
                val historyEntity = RealCgmHistoryEntity()

                historyEntity.eventIndex = rawHistory.timeOffset
                historyEntity.deviceTime = getHistoryDate(rawHistory.timeOffset)
                historyEntity.rawData1 = rawHistory.i1
                historyEntity.rawData2 = rawHistory.i2
                historyEntity.rawData3 = rawHistory.vc
                if (existHistory != null) {
                    historyEntity.recordIndex = existHistory.recordIndex
                    historyEntity.id = existHistory.id
                    historyEntity.idx = existHistory.idx
                    historyEntity.eventWarning = existHistory.eventWarning
                    historyEntity.eventData = existHistory.eventData
                    historyEntity.eventType = existHistory.eventType
                    historyEntity.dataStatus = 1
                }
                historyEntity.deviceId = deviceId
                historyEntity.authorizationId = userId
                historyEntity.updateRecordUUID()
                tempRawList.add(historyEntity)
            }
            cgmHistoryBox!!.put(tempRawList)
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

    fun getGlucoseLevel(glucose: Float?): GlucoseLevel? {
        return when {
            glucose == null -> null
            glucose > ThresholdManager.hyper -> GlucoseLevel.HIGH
            glucose < ThresholdManager.hypo -> GlucoseLevel.LOW
            else -> GlucoseLevel.NORMAL
        }
    }

    /**
     * @param needAlert (是否需要弹框提示，再切换语言重新加载历史数据的时候不需要)
     * */
    fun updateGlucoseTrend(dateTime: Date, needAlert: Boolean = true) {
        ObjectBox.runAsync({
            val size = 5
            val glucoseArray = FloatArray(size)
            var isEventCalibration = false //是否最近有校准事件
            loop@ for (index in cgmHistories.size - 1 downTo 0 step 4) {
                val history = cgmHistories[index]
                if (history.eventWarning == -1) {
                    continue@loop
                }
                val i =
                    ((dateTime.time - history.deviceTime.time).toDouble() / 1000 / 300f).roundToInt()
                val glu = history.eventData ?: 0f
                if (i < 0) {
                    continue@loop
                } else if (i >= size) {
                    break@loop
                } else {
                    if (history.eventType == History.HISTORY_GLUCOSE) {
                        glucoseArray[i] = glu
                    } else {
                        isEventCalibration = true
                        break@loop
                    }
                }
            }
            val roc = if (isEventCalibration) null else {
                if (glucoseArray.any { it < ThresholdManager.hypo || it > ThresholdManager.hyper }) null else {
                    val fn = (1 - exp(
                        (abs(glucoseArray[0] + glucoseArray[2] - 2 * glucoseArray[1]) + abs(
                            glucoseArray[1] + glucoseArray[3] - 2 * glucoseArray[2]
                        ) + abs(glucoseArray[2] + glucoseArray[4] - 2 * glucoseArray[3])) / 5 - 1
                    )) * 1.5
                    if (fn > 0) {
                        val trend =
                            (fn * (glucoseArray[0] - glucoseArray[1]) / 5 + fn * (glucoseArray[0] - glucoseArray[2]) / 10 + (glucoseArray[0] - glucoseArray[3]) / 15) / (1 + 2 * fn);
                        if ((trend > 0.06 && (glucoseArray[0] - glucoseArray[1]) / 5 <= 0) || (trend < -0.06 && (glucoseArray[0] - glucoseArray[1]) / 5 >= 0)) {
                            null
                        } else trend
                    } else null
                }
            }
            when {
                roc == null -> null
                roc > ThresholdManager.SUPER_FAST_UP -> GlucoseTrend.SUPER_FAST_UP
                ThresholdManager.FAST_UP < roc && roc <= ThresholdManager.SUPER_FAST_UP -> GlucoseTrend.FAST_UP
                ThresholdManager.SLOW_UP < roc && roc <= ThresholdManager.FAST_UP -> GlucoseTrend.UP
                roc in ThresholdManager.SLOW_DOWN..ThresholdManager.SLOW_UP -> GlucoseTrend.STEADY
                roc >= ThresholdManager.FAST_DOWN && roc < ThresholdManager.SLOW_DOWN -> GlucoseTrend.DOWN
                roc >= ThresholdManager.SUPER_FAST_DOWN && roc < ThresholdManager.FAST_DOWN -> GlucoseTrend.FAST_DOWN
                roc < ThresholdManager.SUPER_FAST_DOWN -> GlucoseTrend.SUPER_FAST_DOWN
                else -> GlucoseTrend.STEADY
            }.also { glucoseTrend = it }
        }, onSuccess = {
            val time = dateTime.dateHourMinute()
            if (needAlert) {
                if (TimeUtils.currentTimeMillis.millisToSeconds() - dateTime.time.millisToSeconds() > 60 * 30) {
                    return@runAsync
                }
                val frequency = calculateFrequency(MmkvManager.getAlertFrequency())
                when (glucoseTrend) {
                    GlucoseTrend.SUPER_FAST_DOWN -> {
                        if (MmkvManager.isFastDownAlertEnable()) {
                            val lastFastDown: Long = MmkvManager.getLastFastDownAlertTime()
                            if (lastFastDown != 0L
                                && (dateTime.time - lastFastDown).millisToSeconds() < frequency
                            ) {
                                return@runAsync
                            }
                            alert?.invoke("$time", AlertType.MESSAGE_TYPE_GLUCOSEDOWN)
                            MmkvManager.saveFastDownAlertTime(dateTime.time)
                        }
                    }
                    GlucoseTrend.SUPER_FAST_UP -> {
                        if (MmkvManager.isFastUpAlertEnable()) {
                            val lastFastUp: Long = MmkvManager.getLastFastUpAlertTime()
                            if (lastFastUp != 0L
                                && (dateTime.time - lastFastUp).millisToSeconds() < frequency
                            ) {
                                return@runAsync
                            }
                            alert?.invoke("$time", AlertType.MESSAGE_TYPE_GLUCOSEUP)
                            MmkvManager.saveFastUpAlertTime(dateTime.time)
                        }
                    }
                    else -> {
                    }
                }
            }
        }
        )
    }
}