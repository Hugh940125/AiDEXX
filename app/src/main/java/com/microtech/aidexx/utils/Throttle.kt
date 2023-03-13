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

class Throttle {
    fun input() = callbackFlow {
        trySend(Unit).onFailure { e -> e?.printStackTrace() }
        awaitClose { }
    }
}