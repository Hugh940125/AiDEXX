package com.microtech.aidexx.utils

import android.os.SystemClock
import android.view.View
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

/**
 *@date 2023/3/13
 *@author Hugh
 *@desc
 */
fun <T> Flow<T>.throttle(thresholdMillis: Long): Flow<T> = flow {
    var lastTime = 0L
    collect { upstream ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastTime >= thresholdMillis) {
            lastTime = currentTime
            emit(upstream)
        }
    }
}

fun View.clickFlow() = callbackFlow {
    setOnClickListener { trySend(Unit).onFailure { e -> e?.printStackTrace() } }
    awaitClose { setOnClickListener(null) }
}

class Throttle private constructor() {
    private var lastTimeMap = hashMapOf<Int, Long>()

    companion object {
        @Synchronized
        fun instance(): Throttle {
            return Throttle()
        }
    }

    fun emit(thresholdMillis: Long, requestCode: Int, callBack: (() -> Unit)) {
        val currentTime = SystemClock.elapsedRealtime()
        if (lastTimeMap[requestCode] != null) {
            if (currentTime - lastTimeMap[requestCode]!! >= thresholdMillis) {
                callBack.invoke()
                lastTimeMap[requestCode] = currentTime
            }
        } else {
            callBack.invoke()
            lastTimeMap[requestCode] = currentTime
        }
    }
}