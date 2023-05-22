package com.microtech.aidexx.db.entity

import io.objectbox.annotation.BaseEntity
import java.text.SimpleDateFormat
import java.util.TimeZone

@BaseEntity
open class EventTimeInfo {

    var timestamp: Long = 0L

    var appTime: String? = null
        set(value) {
            field = value
            calTimestamp()
        }

    var appTimeZone: String? = null
        set(value) {
            field = value
            calTimestamp()
        }

    var dstOffset: Int? = null
        set(value) {
            field = value
            calTimestamp()
        }

    private fun calTimestamp() {
        if (canCalTimestamp()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.timeZone = TimeZone.getTimeZone(appTimeZone)
            timestamp = sdf.parse(appTime)?.let {
                it.time / 1000
            } ?:let {
                System.currentTimeMillis() / 1000
            }
        }
    }

    private fun canCalTimestamp() = appTime != null && appTimeZone != null && dstOffset != null


}