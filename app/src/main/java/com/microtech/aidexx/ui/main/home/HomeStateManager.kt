package com.microtech.aidexx.ui.main.home

import java.util.*

/**
 *@date 2023/3/9
 *@author Hugh
 *@desc
 */
class HomeStateManager private constructor() {

    private var timer: Timer? = null
    private var currentState = glucosePanel
    private val timeTask = object : TimerTask() {
        override fun run() {
            setState(glucosePanel)
        }
    }

    companion object {
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
        currentState = tag
        onHomeStateChange?.invoke(tag)
    }

    private fun countDownToReset() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(timeTask, 5 * 60 * 1000)
    }
}