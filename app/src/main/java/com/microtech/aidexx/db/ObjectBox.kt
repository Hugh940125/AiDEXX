package com.microtech.aidexx.db

import android.content.Context
import com.microtech.aidexx.db.entity.*
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.awaitCallInTx
import java.util.concurrent.Callable

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
        onError: ((error: Throwable?) -> Unit)? = null
    ) {
        store.runInTxAsync(runnable) { _, error ->
            if (error == null) {
                onSuccess?.invoke()
            } else {
                onError?.invoke(error)
            }
        }
    }

    var userBox: Box<UserEntity>? = null
        get() {
            if (field == null) {
                field = store.boxFor(UserEntity::class.java)
                return field
            }
            return field
        }

    var transmitterBox: Box<TransmitterEntity>? = null
        get() {
            if (field == null) {
                field = store.boxFor(TransmitterEntity::class.java)
                return field
            }
            return field
        }

    var AlertSettingsBox: Box<SettingsEntity>? = null
        get() {
            if (field == null) {
                field = store.boxFor(SettingsEntity::class.java)
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

    var calibrationBox: Box<CalibrateEntity>? = null
        get() {
            if (field == null) {
                field = store.boxFor(CalibrateEntity::class.java)
                return field
            }
            return field
        }

    /**
     * 把ob自带的异步包装到协程中使用
     */
    suspend inline fun <V : Any> awaitCallInTx(callable: Callable<V?>): V? =
        store.awaitCallInTx(callable)

}