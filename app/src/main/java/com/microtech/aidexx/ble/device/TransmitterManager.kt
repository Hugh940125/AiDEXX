package com.microtech.aidexx.ble.device

import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.ble.device.entity.TransmitterEntity_
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ThresholdManager
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransmitterManager private constructor() {
    private var default: TransmitterModel? = null
    var cgmHistories: MutableList<CgmHistoryEntity> = ArrayList()
    var onTransmitterLoaded: ((TransmitterModel) -> Unit)? = null
    var onLoadHistoriesListener: ((List<CgmHistoryEntity>) -> Unit)? = null
    var onUpdateHistoriesListener: ((List<CgmHistoryEntity>) -> Unit)? = null

    suspend fun loadTransmitterFromDb(sn: String? = null): TransmitterEntity? {
        return ObjectBox.store.awaitCallInTx {
            var findFirst: TransmitterEntity? = null
            try {
                val query = transmitterBox!!.query()
                sn?.apply {
                    query.equal(
                            TransmitterEntity_.deviceSn, this, QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                }
                findFirst = query.orderDesc(TransmitterEntity_.idx).build().findFirst()
            } catch (e: Exception) {
                LogUtil.eAiDEX("Failed to load transmitter from db")
            }
            findFirst
        }
    }

    fun removeTransmitterFromDb() {
        ObjectBox.runAsync({
            transmitterBox!!.removeAll()
        }, onSuccess = {
            onLoadHistoriesListener?.invoke(cgmHistories)
        })
    }

    fun buildModel(sn: String): TransmitterModel {
        if (sn != default?.entity?.deviceSn) {
            val entity = TransmitterEntity(sn)
            entity.hyperThreshold = ThresholdManager.hyper
            entity.hypoThreshold = ThresholdManager.hypo
            default = TransmitterModel(entity)
        }
        return default!!
    }

    fun getDefault(): TransmitterModel? {
        if (default == null) {
            AidexxApp.mainScope.launch(Dispatchers.IO) {
                val loadTransmitterFromDb = loadTransmitterFromDb()
                loadTransmitterFromDb?.let {
                    set(TransmitterModel(it))
                }
            }
        }
        return default
    }

    fun removeDefault() {
        default = null
    }

    fun set(model: TransmitterModel) {
        default = model
        model.controller.register()
        onTransmitterLoaded?.invoke(model)
    }

    fun update(model: TransmitterModel) {
        if (model.entity.idx != null) {
            transmitterBox!!.put(model.entity)
        }
    }


//    fun remove(model: CgmModel) {
//
//        model.controller.unregister()
//        transmitters.remove(model.entity.deviceSn)
//
////        onLoadTransmitterListener?.invoke(model)
//    }

    fun updateHistories(cgmHistories: List<CgmHistoryEntity>) {
        this.cgmHistories.addAll(cgmHistories)
        onUpdateHistoriesListener?.invoke(cgmHistories)
    }

    companion object {
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