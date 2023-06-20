package com.microtech.aidexx.ble.device

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.ble.device.entity.CloudDeviceInfo
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.ble.device.model.TransmitterModel
import com.microtech.aidexx.ble.device.model.X_NAME
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.toastShort
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.db.entity.TransmitterEntity_
import com.microtech.aidexx.ui.main.home.HomeStateManager
import com.microtech.aidexx.ui.main.home.needPair
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.widget.dialog.Dialogs
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransmitterManager private constructor() {
    private var default: DeviceModel? = null

    suspend fun loadTransmitter(sn: String? = null) {
        val transmitterEntity: TransmitterEntity? = ObjectBox.store.awaitCallInTx {
            val query = transmitterBox!!.query()
            sn?.apply {
                query.equal(
                    TransmitterEntity_.deviceSn, this, QueryBuilder.StringOrder.CASE_INSENSITIVE
                )
            }
            query.orderDesc(TransmitterEntity_.idx).build().findFirst()
        }
        if (transmitterEntity != null) {
            when (transmitterEntity.deviceType) {
                2 -> {
                    val instance = TransmitterModel.instance(transmitterEntity)
                    instance.observerMessage(instance)
                    set(instance)
                }
            }
        } else {
            getCloudDevice { response ->
                val data = response.data
                data?.let {
                    it.deviceInfo?.let { deviceIfo ->
                        val newEntity = TransmitterEntity()
                        newEntity.id = deviceIfo.deviceId
//                        newEntity.sensorId = deviceIfo.sensorId
//                        newEntity.sensorIndex = deviceIfo.sensorIndex
//                        newEntity.sensorStartTime = deviceIfo.sensorStartUp
                        newEntity.deviceModel = deviceIfo.deviceModel
                        newEntity.deviceSn = deviceIfo.deviceSn
                        newEntity.deviceMac = deviceIfo.deviceMac
                        newEntity.deviceKey = deviceIfo.deviceKey
                        newEntity.et = deviceIfo.et
                        newEntity.deviceName = "$X_NAME-${newEntity.deviceSn}"
                        ObjectBox.runAsync({
                            transmitterBox!!.put(newEntity)
                        }, {
                            when (newEntity.deviceType) {
                                2 -> {
                                    val instance = TransmitterModel.instance(newEntity)
                                    instance.observerMessage(instance)
                                    set(instance)
                                }
                            }
                        })
                    }
                }
            }
            HomeStateManager.instance().setState(needPair)
        }
    }

    private fun getCloudDevice(
        success: ((info: BaseResponse<CloudDeviceInfo>) -> Unit)? = null,
    ) {
        AidexxApp.mainScope.launch {
            withContext(Dispatchers.IO) {
                when (val apiResult = ApiService.instance.getDevice()) {
                    is ApiResult.Success -> {
                        apiResult.result.let { result ->
                            withContext(Dispatchers.Main) {
                                success?.invoke(result)
                            }
                        }
                    }

                    is ApiResult.Failure -> {
                        withContext(Dispatchers.Main) {
                            LogUtil.eAiDEX("get device fail ----> ${apiResult.msg}")
                        }
                    }
                }
            }
        }
    }

    suspend fun removeDb() {
        ObjectBox.awaitCallInTx {
            transmitterBox!!.removeAll()
        }
    }

    fun buildModel(sn: String, address: String): DeviceModel? {
        if (sn == default?.entity?.deviceSn) {
            Dialogs.showError("不可重复配对")
            return null
        }
        val entity = TransmitterEntity(sn)
        entity.deviceMac = address
        return TransmitterModel.instance(entity)
    }

    fun getDefault(): DeviceModel? {
        return default
    }

    fun removeDefault() {
        if (default?.isPaired() == false) {
            default = null
            notifyTransmitterChange(null)
        }
    }

    fun set(model: DeviceModel) {
        default = model
        if (default?.isPaired() == true) {
            model.getController().register()
        }
        notifyTransmitterChange(model)
    }

    fun update(model: TransmitterModel) {
        if (model.entity.idx != null) {
            transmitterBox!!.put(model.entity)
        }
    }

    private fun notifyTransmitterChange(model: DeviceModel?) {
        for (listener in listenerList) {
            listener?.invoke(model)
        }
    }


//    fun remove(model: CgmModel) {
//
//        model.controller.unregister()
//        transmitters.remove(model.entity.deviceSn)
//
////        onLoadTransmitterListener?.invoke(model)
//    }

    fun updateHistories(cgmHistories: List<RealCgmHistoryEntity>) {
    }

    companion object {

        private val listenerList = mutableListOf<((model: DeviceModel?) -> Unit)?>()

        fun setOnTransmitterChangeListener(listener: ((model: DeviceModel?) -> Unit)?) {
            listenerList.add(listener)
        }

        fun removeOnTransmitterChangeListener(listener: ((model: DeviceModel) -> Unit)?) {
            listenerList.remove(listener)
        }

        private var INSTANCE: TransmitterManager? = null
            get() {
                if (field == null) {
                    field = TransmitterManager()
                }
                return field
            }

        @Synchronized
        fun instance(): TransmitterManager {
            return INSTANCE!!
        }
    }
}