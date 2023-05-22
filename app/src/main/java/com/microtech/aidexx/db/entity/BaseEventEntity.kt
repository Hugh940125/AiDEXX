package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.common.formatWithoutZone
import com.microtech.aidexx.utils.LogUtil
import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import io.objectbox.annotation.IndexType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

@BaseEntity
abstract class BaseEventEntity: EventEntity {

    @Id(assignable = true)
    override var idx: Long? = null

    @Index
    override var state: Int = 0

    override var id: String? = null

    @Index(type = IndexType.HASH)
    override var userId: String? = null

    override var time: Date = Date()
    @Index
    override var recordIndex: Long? = null
    override var createTime: Date = Date()
    override var recordId: String? = null
    @Index
    override var deleteStatus: Int = 0  //删除状态 1、待删除：本地删除未同步 2、已删除 本地删除且已同步
    override var language: String = "" // 保存事件时的语言状态
    override var uploadState:Int = 0  //1 待上传 2 已上传


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

    protected fun setTimeInfo(date: Date) {
        appTime = date.formatWithoutZone() // yyyy-MM-dd HH:mm:ss
        appTimeZone = TimeZone.getDefault().id //
        dstOffset = TimeZone.getDefault().dstSavings //
    }

    private fun calTimestamp() {
        if (canCalTimestamp()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.timeZone = TimeZone.getTimeZone(appTimeZone)
            timestamp = sdf.parse(appTime)?.let {
                it.time / 1000
            } ?:let {
                LogUtil.d("calTimestamp error=> $appTime $appTimeZone $dstOffset")
                System.currentTimeMillis() / 1000
            }
        }
    }

    private fun canCalTimestamp() = appTime != null && appTimeZone != null && dstOffset != null

    abstract override fun getEventDescription(res: Resources): String
    abstract override fun getValueDescription(res: Resources): String


}