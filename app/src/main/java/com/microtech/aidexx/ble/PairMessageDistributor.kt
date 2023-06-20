package com.microtech.aidexx.ble

import com.microtechmd.blecomm.entity.BleMessage
import java.util.concurrent.CopyOnWriteArrayList

/**
 *@date 2023/3/7
 *@author Hugh
 *@desc
 */

class PairMessageDistributor {

    private val observerList = CopyOnWriteArrayList<MessageObserver>()

    companion object {
        private var instance: PairMessageDistributor? = null

        @Synchronized
        fun instance(): PairMessageDistributor {
            if (instance == null) {
                instance = PairMessageDistributor()
            }
            return instance!!
        }
    }

    fun observer(observer: MessageObserver) {
        observerList.add(observer)
    }

    fun removeObserver(observer: MessageObserver) {
        observerList.remove(observer)
    }

    fun send(message: BleMessage) {
        for (observer in observerList) {
            observer.onMessage(message)
        }
    }

    fun clear() {
        observerList.clear()
    }
}