package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.common.DATE_FORMAT_YMDHMS
import com.microtech.aidexx.common.formatWithoutZone
import com.microtech.aidexx.utils.LogUtil
import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@BaseEntity
abstract class BaseEventEntity {

    @Id(assignable = true)
    open var idx: Long? = null

    @Index
    open var state: Int = 0

    open var id: String? = null

    @Index(type = IndexType.HASH)
    open var userId: String? = null

    @Index
    open var recordIndex: Long? = null
    open var createTime: Date = Date()
    open var recordId: String? = null
    @Index
    open var deleteStatus: Int = 0  //删除状态 1、待删除：本地删除未同步 2、已删除 本地删除且已同步
    open var language: String = "" // 保存事件时的语言状态
    open var uploadState:Int = 0  //1 待上传 2 已上传


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

    fun getDisplayTime(formatStr: String = "HH:mm", useDeviceTimeZone: Boolean = true): String {
        val sdf = SimpleDateFormat(formatStr, Locale.ENGLISH)

        sdf.timeZone =
            if (useDeviceTimeZone) TimeZone.getDefault()
            else TimeZone.getTimeZone(appTimeZone)

        var str = ""
        try {
            str = sdf.format(timestamp)
        } catch(e: Exception) {
            LogUtil.e("getDisplayTime error $timestamp f=$formatStr useDeviceTimeZone=$useDeviceTimeZone")
        }

        return str
    }

    fun setTimeInfo(date: Date) {
        appTime = date.formatWithoutZone() // yyyy-MM-dd HH:mm:ss
        appTimeZone = TimeZone.getDefault().id //
        dstOffset = TimeZone.getDefault().dstSavings //
    }

    private fun calTimestamp() {
        if (canCalTimestamp()) {
            val sdf = SimpleDateFormat(DATE_FORMAT_YMDHMS, Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone(appTimeZone)
            timestamp = sdf.parse(appTime)?.let {
                it.time
            } ?:let {
                LogUtil.d("calTimestamp error=> $appTime $appTimeZone $dstOffset")
                System.currentTimeMillis()
            }
        }
    }

    private fun canCalTimestamp() = appTime != null && appTimeZone != null && dstOffset != null

    abstract fun getEventDescription(res: Resources): String
    abstract fun getValueDescription(res: Resources): String


}