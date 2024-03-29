package com.microtech.aidexx.ui.main.home.chart

import com.microtech.aidexx.utils.TimeUtils
import java.util.*
import kotlin.math.roundToLong

class                                                                                                                                                                     XAxisUtils {
    companion object {
        private val timeZero: Long = TimeUtils.zeroOfDay(Date().time/1000)

        fun dateToX(date: Date): Float {
            return secondToX(date.time / 1000)
        }

        fun secondToX(second: Long): Float {
            return (second - timeZero).toFloat() / TimeUtils.oneHourSeconds
        }

        fun xToSecond(x: Float): Long {
            return (x * 60).roundToLong() * 60 + timeZero
        }
    }
}