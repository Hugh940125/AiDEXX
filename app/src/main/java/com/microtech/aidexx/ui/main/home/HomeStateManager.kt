package com.microtech.aidexx.ui.main.home

import java.util.*

/**
 *@date 2023/3/9
 *@author Hugh
 *@desc
 */
class HomeStateManager private constructor() {
    private var timer: Timer? = null
    private var timeLeft: Int? = null
    private var currentState = glucosePanel

    init {
        timer = Timer()
    }

    private val timeTask = object : TimerTask() {
        override fun run() {
            setState(glucosePanel)
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
            countDownToReset()
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
        timeTask.cancel()
        timer?.schedule(timeTask, 5 * 60 * 1000)
    }

    fun cancel() {
        timer?.cancel()
        timer = null
    }
}