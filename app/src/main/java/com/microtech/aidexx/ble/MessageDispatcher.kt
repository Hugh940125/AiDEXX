package com.microtech.aidexx.ble

import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

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

    fun dispatch(scope: CoroutineScope, message: BleMessage) {
        scope.launch {
            mutableSharedFlow.emit(message)
        }
    }

    fun observer(scope: CoroutineScope, onReceive: ((message: BleMessage) -> Unit)) {
        scope.launch {
            mutableSharedFlow.buffer(10).flowOn(Dispatchers.IO).collect {
                onReceive.invoke(it)
            }
        }
    }
}