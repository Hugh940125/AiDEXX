package com.microtech.aidexx.ble

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.microtechmd.blecomm.entity.BleMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 *@date 2023/3/7
 *@author Hugh
 *@desc
 */

const val MESSAGE_DISTRIBUTOR_ACTION = "message.distributor.action"

class MessageDistributor {

    private val observerList = CopyOnWriteArrayList<MessageObserver>()
    private var topObserver: MessageObserver? = null

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

    fun observerAndIntercept(observer: MessageObserver) {
        topObserver = observer
    }

    fun removeObserver() {
        topObserver = null
    }

    fun observer(observer: MessageObserver) {
        observerList.add(observer)
    }

    fun removeObserver(observer: MessageObserver) {
        observerList.remove(observer)
    }

    fun send(message: BleMessage) {
        if (topObserver != null) {
            topObserver?.onMessage(message)
            return
        }
        for (observer in observerList) {
            observer.onMessage(message)
        }
    }

    fun clear() {
        observerList.clear()
    }
}