package com.microtech.aidexx.db.entity

import android.content.res.Resources
import java.util.*

interface EventEntity {
    var idx: Long?
    var state: Int
    var id: String?
    var authorizationId: String?
    var time: Date
    var recordIndex: Long?
    var deleteStatus: Int  //删除状态 1、待删除：本地删除未同步 2、已删除 本地删除且已同步
    fun getEventDescription(res: Resources): String
    fun getValueDescription(res: Resources): String
}