package com.microtech.aidexx.utils

import java.util.*

class TimeUtils {
    companion object {
        const val oneHourSeconds = 3600
        const val oneDaySeconds = oneHourSeconds * 24
        const val oneDayMillis = oneDaySeconds * 1000
        var currentTimeMillis: Long = 0
            get() {
                if (field == 0L) {
                    field = Date().time
                }
                return field
            }

        fun zeroOfDay(second: Long): Long {
            val offset =
                timeZoneOffsetSeconds()
            return (second + offset) / oneDaySeconds * oneDaySeconds - offset
        }

        fun timeZoneOffsetSeconds(): Long {
            return TimeZone.getDefault().rawOffset / 1000L
        }
    }
}