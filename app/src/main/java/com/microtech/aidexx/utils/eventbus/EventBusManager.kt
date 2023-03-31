package com.microtech.aidexx.utils.eventbus

import androidx.lifecycle.LifecycleOwner
import com.jeremyliao.liveeventbus.LiveEventBus

/**
 *@date 2023/2/9
 *@desc EventBus统一入口，方便更换维护
 */
object EventBusManager {

    fun <T> send(key: String, value: T) {
        LiveEventBus.get<T>(key).post(value)
    }

    fun <T> sendDelay(key: String, value: T, delay: Long) {
        LiveEventBus.get<T>(key).postDelay(value, delay)
    }

    fun <T : Any?> onReceive(
        vararg keys: String,
        owner: LifecycleOwner,
        callback: ((it: T) -> Unit)
    ) {
        for (key in keys) {
            LiveEventBus.get<T>(key).observe(owner) {
                callback.invoke(it)
            }
        }
    }

    fun <T : Any?> onReceive(key: String, owner: LifecycleOwner, callback: ((it: T) -> Unit)) {
        LiveEventBus.get<T>(key).observe(owner) {
            callback.invoke(it)
        }
    }
}