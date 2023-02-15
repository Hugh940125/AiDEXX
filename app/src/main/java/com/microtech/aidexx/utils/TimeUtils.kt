package com.microtech.aidexx.utils

import java.util.*

class                                                                                                                                                                     TimeUtils {
    companion object {
        const val oneHour = 3600
        const val oneDay = oneHour * 24

        fun zeroOfDay(second: Long): Long {
            val offset =
                timeZoneOffsetSeconds()
            return (second + offset) / oneDay * oneDay - offset
        }

        fun timeZoneOffsetSeconds(): Long {
            return TimeZone.getDefault().rawOffset/1000L
        }
    }
}