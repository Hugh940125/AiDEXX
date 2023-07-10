package com.microtech.aidexx.utils

import com.microtech.aidexx.common.DATE_FORMAT_YMDHMS
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtils {
    const val oneDayHour = 24
    const val oneHourSeconds = 3600L
    const val oneDaySeconds = oneHourSeconds * 24
    const val oneDayMillis = oneDaySeconds * 1000
    const val oneMinuteMillis = 60 * 1000L
    var currentTimeMillis: Long = 0
        get() {
            field = Date().time
            return field
        }

    var currentDate: Date = Date()
        get() {
            field = Date()
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

    fun Date.dateHourMinute(pattern: String = "MM-dd HH:mm"): String =
        SimpleDateFormat(pattern, Locale.getDefault()).format(this)

    fun getTimeZoneId(): String? {
        return TimeZone.getDefault().id
    }

    fun calTimestamp(yyyyMMddHHmmss: String, timeZone: String, useDaylightTime: Boolean): Long? {
        val sdf = SimpleDateFormat(DATE_FORMAT_YMDHMS, Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone(timeZone)
        return yyyyMMddHHmmss.let {
            sdf.parse(it)?.time?.plus(
                // 加上夏令时
                if (useDaylightTime) TimeZone.getDefault().dstSavings else 0
            )
        }
    }

}