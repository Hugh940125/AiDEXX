package com.microtech.aidexx.ble

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.microtechmd.blecomm.entity.BleMessage

/**
 *@date 2023/3/7
 *@author Hugh
 *@desc
 */

const val MESSAGE_DISTRIBUTOR_ACTION = "message.distributor.action"

class MessageDistributor {

    private lateinit var localBroadcastManager: LocalBroadcastManager

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

    fun registerObserver(observer: MessageObserver) {

    }

    fun unregisterObserver(observer: MessageObserver) {

    }

    fun unregisterAll(){

    }

    fun send(message: BleMessage) {
        val intent = Intent(MESSAGE_DISTRIBUTOR_ACTION).apply {
            putExtra("message", message)
        }
        localBroadcastManager.sendBroadcast(intent)
    }
}