package com.microtech.aidexx.db

import android.content.Context
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.MyObjectBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
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

    var cgmHistoryBox: Box<RealCgmHistoryEntity>? = null
        get() {
            if (field == null) {
                field = store.boxFor(RealCgmHistoryEntity::class.java)
                return field
            }
            return field
        }

    var bgHistoryBox: Box<BloodGlucoseEntity>? = null
        get() {
            if (field == null) {
                field = store.boxFor(BloodGlucoseEntity::class.java)
                return field
            }
            return field
        }
}