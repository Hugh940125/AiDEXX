package com.microtech.aidexx.ble

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.microtechmd.blecomm.entity.BleMessage
import java.util.concurrent.ConcurrentHashMap

/**
 *@date 2023/3/7
 *@author Hugh
 *@desc
 */

const val MESSAGE_DISTRIBUTOR_ACTION = "message.distributor.action"

class MessageDistributor {

    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val concurrentHashMap = ConcurrentHashMap<Int, MessageObserver>()

    fun init(context: Context) {
        localBroadcastManager = LocalBroadcastManager.getInstance(context)
    }

    companion object {
        private var instance: MessageDistributor? = null

        @Synchronized
        fun instance(): MessageDistributor {
            if (instance == null) {
                instance = MessageDistributor()
            }
            return instance!!
        }
    }

    fun registerObserver(observer: MessageObserver, priority: Int) {
        concurrentHashMap[priority] = observer
        sort()
    }

    fun unregisterObserver(observer: MessageObserver, priority: Int) {
        concurrentHashMap.remove(priority, observer)
        sort()
    }

    private fun sort() {
        concurrentHashMap.toSortedMap(compareByDescending { it })
    }

    fun send(message: BleMessage) {
        val priorityMax = concurrentHashMap.keys.first()
        for ((priority, observer) in concurrentHashMap) {
            if (priority == priorityMax) {
                observer.onMessage(message)
            } else {
                break
            }
        }
    }
}