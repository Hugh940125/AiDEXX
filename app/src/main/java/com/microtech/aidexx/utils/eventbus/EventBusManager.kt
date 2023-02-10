package com.microtech.aidexx.utils.eventbus

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 *@date 2023/2/9
 *@desc EventBus统一入口，方便更换维护
 */
object EventBusManager {

    fun <T : Any?> send(key: String, clazz: Class<T>, value: T) {
        LiveEventBus.get(key, clazz).post(value)
    }

    fun <T : Any?> onReceive(
        key: String,
        clazz: Class<T>,
        owner: LifecycleOwner,
        callback: ((it: T) -> Unit)
    ) {
        LiveEventBus.get(key, clazz).observe(owner) {
            callback.invoke(it)
        }
    }
}