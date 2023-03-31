package com.microtech.aidexx.ble

import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 *@date 2023/3/7
 *@author Hugh
 *@desc
 */
class MessageDispatcher {
    private var needSend = true
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

    fun observeLifecycle(scope: CoroutineScope) {
        mutableSharedFlow.subscriptionCount
            .map { count -> count > 0 }
            .distinctUntilChanged()
            .onEach { isActive ->
                if (isActive != needSend) {
                    needSend = isActive
                }
            }
            .launchIn(scope)
    }

    fun dispatch(scope: CoroutineScope, message: BleMessage) {
//        if (needSend) {
        scope.launch {
            mutableSharedFlow.emit(message)
//            }
        }
    }

    fun observer(scope: CoroutineScope, onReceive: ((message: BleMessage) -> Unit)): Job {
        return scope.launch {
            mutableSharedFlow.buffer(10).flowOn(Dispatchers.IO).collect {
                onReceive.invoke(it)
            }
        }
    }
}