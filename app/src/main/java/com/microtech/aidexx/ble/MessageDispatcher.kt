package com.microtech.aidexx.ble

import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn

/**
 *@date 2023/3/7
 *@author Hugh
 *@desc
 */
class MessageDispatcher {

    private val mutableSharedFlow = MutableSharedFlow<BleMessage>()

    companion object {
        private var instance: MessageDispatcher? = null

        @Synchronized
        fun instance(): MessageDispatcher {
            if (instance == null) {
                instance = MessageDispatcher()
            }
            return instance!!
        }
    }

    suspend fun dispatch(message: BleMessage) {
        mutableSharedFlow.emit(message)
    }

    suspend fun Obsever(onReceive: ((message: BleMessage) -> Unit)) {
        mutableSharedFlow.buffer(10).flowOn(Dispatchers.IO).collect {
            onReceive.invoke(it)
        }
    }
}