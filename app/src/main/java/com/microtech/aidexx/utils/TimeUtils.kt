package com.microtech.aidexx.utils

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    const val oneHourSeconds = 3600
    const val oneDaySeconds = oneHourSeconds * 24
    const val oneDayMillis = oneDaySeconds * 1000
    var currentTimeMillis: Long = 0
        get() {
            field = Date().time
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

    fun Date.dateHourMinute(pattern: String = "MM-dd HH:mm"): String? =
        SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}