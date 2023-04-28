package com.microtech.aidexx.data

import android.os.SystemClock

/**
 *@date 2023/4/27
 *@author Hugh
 *@desc
 */
object UploadControl {
    private var currentIndex = 0
    private var nextTime = 0L
    private val uploadInterval = listOf(1, 1, 4, 4, 10, 10, 30, 60, 120)

    private fun calculateNextTime(): Long {
        return SystemClock.elapsedRealtime() + uploadInterval[currentIndex] * 30 * 1000
    }

    private fun setNextTime() {
        currentIndex++
        if (currentIndex >= uploadInterval.size) {
            currentIndex = uploadInterval.size - 1
        }
        nextTime = calculateNextTime()
    }

    fun executable(): Boolean {
        if (SystemClock.elapsedRealtime() < nextTime) {
            return false
        }
        setNextTime()
        return true
    }

    fun reset() {
        currentIndex = 0
        nextTime = 0L
    }
}