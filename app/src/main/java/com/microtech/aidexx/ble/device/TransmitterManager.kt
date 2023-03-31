package com.microtech.aidexx.ble.device

import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.ble.device.model.TransmitterModel
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.db.entity.TransmitterEntity_
import com.microtech.aidexx.utils.ThresholdManager
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.query.QueryBuilder

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
        transmitterEntity?.let {
            when (transmitterEntity.deviceType) {
                2 -> set(TransmitterModel.instance(transmitterEntity))
            }
        }
    }

    suspend fun removeDb() {
        ObjectBox.awaitCallInTx {
            transmitterBox!!.removeAll()
        }
    }

    fun buildModel(sn: String, address: String): DeviceModel {
        if (sn != default?.entity?.deviceSn) {
            val entity = TransmitterEntity(sn)
            entity.deviceMac = address
            entity.hyperThreshold = ThresholdManager.hyper
            entity.hypoThreshold = ThresholdManager.hypo
            default = TransmitterModel.instance(entity)
        }
        default?.let {
            notifyTransmitterChange(it)
        }
        return default!!
    }

    fun getDefault(): DeviceModel? {
        return default
    }

    fun removeDefault() {
        default = null
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

    fun notifyTransmitterChange(model: DeviceModel) {
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

        private val listenerList = mutableListOf<((model: DeviceModel) -> Unit)?>()

        fun setOnTransmitterChangeListener(listener: ((model: DeviceModel) -> Unit)?) {
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