package com.microtech.aidexx.db

import android.content.Context
import com.microtech.aidexx.MyObjectBox
import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import io.objectbox.Box
import io.objectbox.BoxStore

/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
object ObjectBox {
    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }

    fun runAsync(
        runnable: Runnable,
        onSuccess: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        store.runInTxAsync(runnable) { _, error ->
            if (error == null) {
                onSuccess?.invoke()
            } else {
                onError?.invoke()
            }
        }
    }

    var transmitterBox: Box<TransmitterEntity>? = null
        get() {
            if (field == null) {
                field = store.boxFor(TransmitterEntity::class.java)
                return field
            }
            return field
        }

    var cgmHistoryBox: Box<CgmHistoryEntity>? = null
        get() {
            if (field == null) {
                field = store.boxFor(CgmHistoryEntity::class.java)
                return field
            }
            return field
        }
}