package com.microtech.aidexx.ui.main.home

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import java.util.*

/**
 *@date 2023/3/9
 *@author Hugh
 *@desc
 */

private const val RESET_HOME_STATE: Int = 110

class HomeStateManager private constructor() {
    private var timer: Timer? = null
    private var timeLeft: Int? = null
    private val handler: Handler
    private var currentState = glucosePanel

    init {
        handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                setState(glucosePanel)
            }
        }
    }

    companion object {
        var onWarmingUpTimeLeftListener: ((timeLeft: Int?) -> Unit)? = null
        var onHomeStateChange: ((tag: String) -> Unit)? = null
        private var homeStateManager: HomeStateManager? = null

        @Synchronized
        fun instance(): HomeStateManager {
            if (homeStateManager == null) {
                homeStateManager = HomeStateManager()
            }
            return homeStateManager!!
        }
    }

    fun setState(tag: String) {
        if (tag != glucosePanel) {
            if (tag != needPair){
                countDownToReset()
            }
            EventBusManager.send(EventBusKey.UPDATE_NOTIFICATION, false)
        }
        if (tag == warmingUp) {
            timeLeft = null
        }
        currentState = tag
        onHomeStateChange?.invoke(tag)
    }

    fun setWarmingUpTimeLeft(time: Int) {
        if (timeLeft == time) {
            return
        }
        timeLeft = time
        onWarmingUpTimeLeftListener?.invoke(timeLeft)
    }

    @Synchronized
    private fun countDownToReset() {
        handler.removeMessages(RESET_HOME_STATE)
        handler.sendEmptyMessageDelayed(RESET_HOME_STATE, 5 * 60 * 1000)
    }

    fun cancel() {
        timer?.cancel()
        timer = null
    }
}